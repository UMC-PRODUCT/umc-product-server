package com.umc.product.member.application.service;

import com.umc.product.authentication.application.port.in.command.OAuthAuthenticationUseCase;
import com.umc.product.authentication.application.port.in.command.dto.LinkOAuthCommand;
import com.umc.product.global.exception.NotImplementedException;
import com.umc.product.member.application.port.in.command.ManageMemberUseCase;
import com.umc.product.member.application.port.in.command.dto.DeleteMemberCommand;
import com.umc.product.member.application.port.in.command.dto.RegisterMemberCommand;
import com.umc.product.member.application.port.in.command.dto.UpdateMemberCommand;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.terms.application.port.in.command.ManageTermsAgreementUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService implements ManageMemberUseCase {

    private final SaveMemberPort saveMemberPort;
    private final OAuthAuthenticationUseCase oAuthAuthenticationUseCase;
    private final ManageTermsAgreementUseCase manageTermsAgreementUseCase;


    @Override
    @Transactional
    public Long registerMember(RegisterMemberCommand command) {
        // TODO: 학교 정보 validation 필요
        // TODO: profileImage 처리 필요

        Member savedMember = saveMemberPort.save(
                command.toEntity()
        );

        // OAuth 게정 정보 저장
        oAuthAuthenticationUseCase.linkOAuth(
                LinkOAuthCommand.builder()
                        .memberId(savedMember.getId())
                        .provider(command.provider())
                        .providerId(command.providerId())
                        .build()
        );

        // TODO: 필수 약관들을 모두 동의했는지를 검증하는 로직 추가 필요

        // 약관 동의 정보 저장
        command.termConsents().forEach(termConsent ->
                manageTermsAgreementUseCase.createTermConsent(
                        termConsent.toCommand(savedMember.getId())
                )
        );

        return savedMember.getId();
    }

    @Override
    public void updateMember(UpdateMemberCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteMember(DeleteMemberCommand command) {
        throw new NotImplementedException();
    }
}
