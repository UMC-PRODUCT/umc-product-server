package com.umc.product.member.application.service;

import com.umc.product.member.application.port.in.command.ManageMemberCredentialUseCase;
import com.umc.product.member.application.port.in.command.dto.ChangeMemberPasswordCommand;
import com.umc.product.member.application.port.in.command.dto.RegisterMemberCredentialCommand;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberCredentialCommandService implements ManageMemberCredentialUseCase {

    private final LoadMemberPort loadMemberPort;

    @Override
    public void registerCredential(RegisterMemberCredentialCommand command) {
        Member member = loadMemberPort.findById(command.memberId())
            .orElseThrow(() -> new MemberDomainException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 도메인 메서드가 활성 상태/중복 등록을 모두 검증한다.
        // JPA 영속성 컨텍스트가 변경 감지(dirty checking) 로 저장한다.
        member.registerCredential(command.loginId(), command.encodedPassword());
    }

    @Override
    public void changePassword(ChangeMemberPasswordCommand command) {
        Member member = loadMemberPort.findById(command.memberId())
            .orElseThrow(() -> new MemberDomainException(MemberErrorCode.MEMBER_NOT_FOUND));

        member.changePassword(command.encodedPassword());
    }
}
