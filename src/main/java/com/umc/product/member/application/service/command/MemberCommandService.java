package com.umc.product.member.application.service.command;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.member.application.port.in.command.CompleteRegisterMemberCommand;
import com.umc.product.member.application.port.in.command.ManageMemberUseCase;
import com.umc.product.member.application.port.in.command.RegisterMemberCommand;
import com.umc.product.member.application.port.out.LoadMemberOAuthPort;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.MemberOAuth;
import com.umc.product.member.domain.MemberTermAgreement;
import com.umc.product.member.domain.exception.MemberErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberCommandService implements ManageMemberUseCase {

    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveUserPort;
    private final LoadMemberOAuthPort loadMemberOAuthPort;

    @Override
    public Long register(RegisterMemberCommand command) {
        // 1. OAuth 정보로 이미 등록된 사용자인지 확인
        loadMemberOAuthPort
                .findByProviderAndProviderUserId(command.oauthProvider(), command.providerId())
                .ifPresent(oauth -> {
                    throw new BusinessException(Domain.MEMBER,
                            MemberErrorCode.MEMBER_ALREADY_EXISTS);
                });

        // 2. 이메일 중복 확인
        if (loadMemberPort.existsByEmail(command.email())) {
            throw new BusinessException(Domain.MEMBER, MemberErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 4. Member 생성 및 저장
        Member member = command.toMemberEntity();
        Member savedMember = saveUserPort.save(member);

        // 5. UserOAuth 저장
        MemberOAuth memberOAuth = command.toMemberOAuthEntity(savedMember.getId());
        saveUserPort.saveOAuth(memberOAuth);

        // 6. 약관 동의 정보 저장
        List<MemberTermAgreement> agreements = command.toTermAgreementEntities(savedMember.getId());
        saveUserPort.saveTermAgreements(agreements);

        // 7. User 상태를 ACTIVE로 변경
        savedMember.activate();

        return savedMember.getId();
    }

    @Override
    public Long completeRegister(CompleteRegisterMemberCommand command) {
        // 1. Command에 담긴 회원이 존재하고, 상태가 PENDING인지 확인
        Member member = loadMemberPort.findById(command.memberId())
                .orElseThrow(() -> new BusinessException(Domain.MEMBER,
                        MemberErrorCode.MEMBER_NOT_FOUND));
        member.validateIfRegisterAvailable();

        // 2. 이메일 중복 확인 및 이메일 인증이 완료되었는지 확인
        // TODO: 이메일 인증 로직 추후에 붙이도록 함

        // 3. Member 업데이트 (Status ACTIVE 전환 포함)
        member.activate();
        member.updateProfile(command.nickname(), command.schoolId(), command.profileImageId());
        Member savedMember = saveUserPort.save(member);

        // 4. 약관 동의 정보 저장
        List<MemberTermAgreement> agreements = command.toTermAgreementEntities(savedMember.getId());
        saveUserPort.saveTermAgreements(agreements);

        return savedMember.getId();
    }
}
