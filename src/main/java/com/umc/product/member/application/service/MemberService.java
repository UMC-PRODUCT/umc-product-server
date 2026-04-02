package com.umc.product.member.application.service;

import com.umc.product.audit.domain.AuditAction;
import com.umc.product.audit.domain.AuditLogEvent;
import com.umc.product.authentication.application.port.in.command.OAuthAuthenticationUseCase;
import com.umc.product.authentication.application.port.in.command.dto.LinkOAuthCommand;
import com.umc.product.authentication.application.port.in.command.dto.UnlinkOAuthCommand;
import com.umc.product.authentication.application.port.in.query.GetMemberOAuthUseCase;
import com.umc.product.authentication.application.port.in.query.dto.MemberOAuthInfo;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.member.application.port.in.command.ManageMemberUseCase;
import com.umc.product.member.application.port.in.command.dto.DeleteMemberCommand;
import com.umc.product.member.application.port.in.command.dto.RegisterMemberCommand;
import com.umc.product.member.application.port.in.command.dto.TermConsents;
import com.umc.product.member.application.port.in.command.dto.UpdateMemberCommand;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;
import com.umc.product.notification.application.port.in.SendWebhookAlarmUseCase;
import com.umc.product.notification.application.port.in.dto.SendWebhookAlarmCommand;
import com.umc.product.notification.domain.WebhookPlatform;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.organization.application.port.out.query.LoadSchoolPort;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.term.application.port.in.command.ManageTermAgreementUseCase;
import com.umc.product.term.application.port.in.query.GetTermUseCase;
import com.umc.product.term.domain.exception.TermDomainException;
import com.umc.product.term.domain.exception.TermErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService implements ManageMemberUseCase {

    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final LoadSchoolPort loadSchoolPort;

    private final GetFileUseCase getFileUseCase;
    private final OAuthAuthenticationUseCase oAuthAuthenticationUseCase;
    private final GetMemberOAuthUseCase getMemberOAuthUseCase;
    private final ManageTermAgreementUseCase manageTermAgreementUseCase;
    private final GetTermUseCase getTermUseCase;

    private final ApplicationEventPublisher eventPublisher;
    private final SendWebhookAlarmUseCase sendWebhookAlarmUseCase;
    private final GetSchoolUseCase getSchoolUseCase;

    @Override
    @Transactional
    public Long registerMember(RegisterMemberCommand command) {
        validateSchoolExists(command.schoolId());
        validateProfileImageExists(command.profileImageId());
        validateMandatoryTermsAgreed(command);

        Member savedMember = saveMemberPort.save(command.toEntity());

        // OAuth 계정 정보 저장
        oAuthAuthenticationUseCase.linkOAuth(
            LinkOAuthCommand.builder()
                .memberId(savedMember.getId())
                .provider(command.provider())
                .providerId(command.providerId())
                .appleRefreshToken(command.appleRefreshToken())
                .build()
        );

        // 약관 동의 정보 저장
        command.termConsents().forEach(termConsent ->
            manageTermAgreementUseCase.createTermConsent(
                termConsent.toCommand(savedMember.getId())
            )
        );

        String logDescription = getSchoolUseCase.getSchoolDetail(command.schoolId()).schoolName()
            + "소속 " + command.nickname() + "/" + command.name() +
            " 님이 회원 가입하셨습니다.";

        sendWebhookAlarmUseCase.sendBuffered(
            SendWebhookAlarmCommand.builder()
                .platforms(List.of(WebhookPlatform.TELEGRAM))
                .title("새로운 회원이 가입하였습니다.")
                .content(logDescription)
                .build()
        );

        eventPublisher.publishEvent(
            AuditLogEvent.builder()
                .domain(Domain.MEMBER)
                .action(AuditAction.REGISTER)
                .targetType("Member")
                .targetId(String.valueOf(savedMember.getId()))
                .description(logDescription)
                .build()
        );

        return savedMember.getId();
    }

    @Override
    @Transactional
    public List<Long> registerMembers(List<RegisterMemberCommand> commands) {
        commands.forEach(command -> {
            validateSchoolExists(command.schoolId());
            validateProfileImageExists(command.profileImageId());
            validateMandatoryTermsAgreed(command);
        });

        List<Member> savedMembers = saveMemberPort.saveAll(commands.stream()
            .map(RegisterMemberCommand::toEntity)
            .toList()
        );

        // OAuth 계정 정보 벌크 저장
        List<LinkOAuthCommand> oAuthCommands = new ArrayList<>();
        for (int i = 0; i < savedMembers.size(); i++) {
            Member savedMember = savedMembers.get(i);
            RegisterMemberCommand command = commands.get(i);

            oAuthCommands.add(
                LinkOAuthCommand.builder()
                    .memberId(savedMember.getId())
                    .provider(command.provider())
                    .providerId(command.providerId())
                    .build()
            );

            // 약관 동의 정보 저장
            command.termConsents().forEach(termConsent ->
                manageTermAgreementUseCase.createTermConsent(
                    termConsent.toCommand(savedMember.getId())
                )
            );
        }
        oAuthAuthenticationUseCase.linkOAuthBulk(oAuthCommands);

        return savedMembers.stream()
            .map(Member::getId)
            .toList();
    }

    @Override
    @Transactional
    public void updateMember(UpdateMemberCommand command) {
        Member member = findById(command.memberId());

        validateProfileImageExists(command.newProfileImageId());

        member.updateProfile(command.newProfileImageId());
    }

    @Override
    @Transactional
    public void deleteMember(DeleteMemberCommand command) {
        Long memberId = command.memberId();

        Member memberToDelete = loadMemberPort.findById(memberId)
            .orElseThrow(() -> new MemberDomainException(MemberErrorCode.MEMBER_NOT_FOUND));

        List<MemberOAuthInfo> linkedOAuths = getMemberOAuthUseCase.getOAuthList(memberId);

        // TODO: N+1 문제 해결 필요
        for (MemberOAuthInfo oAuthInfo : linkedOAuths) {
            oAuthAuthenticationUseCase.unlinkOAuth(
                UnlinkOAuthCommand.builder()
                    .memberId(memberId)
                    .memberOAuthId(oAuthInfo.memberOAuthId())
                    .isWithdrawal(true)
                    .googleAccessToken(command.googleAccessToken())
                    .kakaoAccessToken(command.kakaoAccessToken())
                    .build()
            );
        }

        saveMemberPort.delete(memberToDelete);

        eventPublisher.publishEvent(
            AuditLogEvent.builder()
                .domain(Domain.MEMBER)
                .action(AuditAction.WITHDRAW)
                .targetType("Member")
                .targetId(String.valueOf(memberId))
                .description(
                    getSchoolUseCase.getSchoolDetail(memberToDelete.getSchoolId()).schoolName()
                        + "소속 " + memberToDelete.getNickname() + "/" + memberToDelete.getName() +
                        " 님이 회원 탈퇴하셨습니다.")
                .build()
        );
    }

    private void validateMandatoryTermsAgreed(RegisterMemberCommand command) {
        Set<Long> requiredTermIds = getTermUseCase.getRequiredTermIds();

        Set<Long> agreedTermIds = command.termConsents().stream()
            .filter(TermConsents::isAgreed)
            .map(TermConsents::termId)
            .collect(Collectors.toSet());

        if (!agreedTermIds.containsAll(requiredTermIds)) {
            throw new TermDomainException(TermErrorCode.MANDATORY_TERMS_NOT_AGREED);
        }
    }

    private Member findById(Long memberId) {
        return loadMemberPort.findById(memberId).orElseThrow(
            () -> new MemberDomainException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    /**
     * 사진 ID가 주어진 경우 해당 파일이 존재하는지 확인
     */
    private void validateProfileImageExists(String profileImageId) {
        if (profileImageId != null) {
            getFileUseCase.throwIfNotExists(profileImageId);
        }
    }

    private void validateSchoolExists(Long schoolId) {
        loadSchoolPort.throwIfNotExists(schoolId);
    }
}
