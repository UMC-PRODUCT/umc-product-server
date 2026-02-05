package com.umc.product.member.application.service;

import com.umc.product.authentication.application.port.in.command.OAuthAuthenticationUseCase;
import com.umc.product.authentication.application.port.in.command.dto.LinkOAuthCommand;
import com.umc.product.global.exception.NotImplementedException;
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
import com.umc.product.organization.application.port.out.query.LoadSchoolPort;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.storage.domain.exception.StorageErrorCode;
import com.umc.product.storage.domain.exception.StorageException;
import com.umc.product.terms.application.port.in.command.ManageTermsAgreementUseCase;
import com.umc.product.terms.application.port.in.query.GetTermsUseCase;
import com.umc.product.terms.domain.exception.TermsDomainException;
import com.umc.product.terms.domain.exception.TermsErrorCode;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
    private final ManageTermsAgreementUseCase manageTermsAgreementUseCase;
    private final GetTermsUseCase getTermsUseCase;


    @Override
    @Transactional
    public Long registerMember(RegisterMemberCommand command) {
        // 학교 ID 존재 검증
        if (!loadSchoolPort.existsById(command.schoolId())) {
            throw new OrganizationDomainException(OrganizationErrorCode.SCHOOL_NOT_FOUND);
        }

        // ProfileImageId 존재 검증 (선택적 필드이므로 null이 아닐 때만 검증)
        validateProfileImageExists(command.profileImageId());

        // 필수 동의 약관에 전부 동의했는지 확인
        validateMandatoryTermsAgreed(command);

        Member savedMember = saveMemberPort.save(command.toEntity());

        // OAuth 계정 정보 저장
        oAuthAuthenticationUseCase.linkOAuth(
            LinkOAuthCommand.builder()
                .memberId(savedMember.getId())
                .provider(command.provider())
                .providerId(command.providerId())
                .build()
        );

        // 약관 동의 정보 저장
        command.termConsents().forEach(termConsent ->
            manageTermsAgreementUseCase.createTermConsent(
                termConsent.toCommand(savedMember.getId())
            )
        );

        return savedMember.getId();
    }

    @Override
    @Transactional
    public void updateMember(UpdateMemberCommand command) {
        Member member = findById(command.memberId());

        validateProfileImageExists(command.newProfileImageId());

        member.updateProfile(command.newProfileImageId());
    }

    @Override
    public void deleteMember(DeleteMemberCommand command) {
        throw new NotImplementedException();
    }

    private void validateMandatoryTermsAgreed(RegisterMemberCommand command) {
        Set<Long> requiredTermIds = getTermsUseCase.getRequiredTermIds();

        Set<Long> agreedTermIds = command.termConsents().stream()
            .filter(TermConsents::isAgreed)
            .map(TermConsents::termId)
            .collect(Collectors.toSet());

        if (!agreedTermIds.containsAll(requiredTermIds)) {
            throw new TermsDomainException(TermsErrorCode.MANDATORY_TERMS_NOT_AGREED);
        }
    }

    private Member findById(Long memberId) {
        return loadMemberPort.findById(memberId).orElseThrow(
            () -> new MemberDomainException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    private void validateProfileImageExists(String profileImageId) {
        if (profileImageId != null && !getFileUseCase.existsById(profileImageId)) {
            throw new StorageException(StorageErrorCode.FILE_NOT_FOUND);
        }
    }
}
