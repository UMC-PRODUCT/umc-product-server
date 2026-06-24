package com.umc.product.member.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.audit.application.port.in.annotation.Audited;
import com.umc.product.audit.domain.AuditAction;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.member.application.port.in.command.ChangeMemberEmailUseCase;
import com.umc.product.member.application.port.in.command.dto.ChangeMemberEmailCommand;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberEmailCommandService implements ChangeMemberEmailUseCase {

    private final LoadMemberPort loadMemberPort;

    @Audited(
        domain = Domain.MEMBER,
        action = AuditAction.UPDATE,
        targetType = "MemberEmail",
        targetId = "#command.memberId()",
        description = "'회원 이메일을 변경했습니다.'"
    )
    @Override
    public void changeEmail(ChangeMemberEmailCommand command) {
        Member member = loadMemberPort.findByIdForUpdate(command.memberId())
            .orElseThrow(() -> new MemberDomainException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (member.getEmail().equals(command.email())) {
            return;
        }

        if (loadMemberPort.existsByEmail(command.email())) {
            throw new MemberDomainException(MemberErrorCode.EMAIL_ALREADY_EXISTS);
        }

        member.changeEmail(command.email());
    }
}
