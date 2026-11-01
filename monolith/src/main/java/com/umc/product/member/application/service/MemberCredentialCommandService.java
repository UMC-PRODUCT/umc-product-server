package com.umc.product.member.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.audit.application.port.in.annotation.Audited;
import com.umc.product.audit.domain.AuditAction;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.member.application.port.in.command.LockMemberCredentialUseCase;
import com.umc.product.member.application.port.in.command.ManageMemberCredentialUseCase;
import com.umc.product.member.application.port.in.command.dto.ChangeMemberPasswordCommand;
import com.umc.product.member.application.port.in.command.dto.MemberCredentialStatusInfo;
import com.umc.product.member.application.port.in.command.dto.RegisterMemberCredentialByEmailCommand;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberCredentialCommandService implements ManageMemberCredentialUseCase, LockMemberCredentialUseCase {

    private final LoadMemberPort loadMemberPort;

    @Audited(
        domain = Domain.MEMBER,
        action = AuditAction.CREATE,
        targetType = "MemberCredential",
        targetId = "#command.memberId()",
        description = "'회원 자격증명을 등록했습니다.'"
    )
    @Override
    public void registerCredentialByEmail(RegisterMemberCredentialByEmailCommand command) {
        Member member = loadMemberPort.findById(command.memberId())
            .orElseThrow(() -> new MemberDomainException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 도메인 메서드가 활성 상태 / 중복 등록을 모두 검증한다.
        // JPA 영속성 컨텍스트가 변경 감지(dirty checking) 로 저장한다.
        member.registerCredential(command.encodedPassword());
    }

    @Audited(
        domain = Domain.MEMBER,
        action = AuditAction.UPDATE,
        targetType = "MemberCredential",
        targetId = "#command.memberId()",
        description = "'회원 비밀번호를 변경했습니다.'"
    )
    @Override
    public void changePassword(ChangeMemberPasswordCommand command) {
        Member member = loadMemberPort.findById(command.memberId())
            .orElseThrow(() -> new MemberDomainException(MemberErrorCode.MEMBER_NOT_FOUND));

        member.changePassword(command.encodedPassword());
    }

    @Override
    public MemberCredentialStatusInfo getCredentialStatusForUpdate(Long memberId) {
        if (memberId == null) {
            throw new MemberDomainException(MemberErrorCode.MEMBER_NOT_FOUND);
        }
        return loadMemberPort.findByIdForUpdate(memberId)
            .map(MemberCredentialStatusInfo::from)
            .orElseThrow(() -> new MemberDomainException(MemberErrorCode.MEMBER_NOT_FOUND));
    }
}
