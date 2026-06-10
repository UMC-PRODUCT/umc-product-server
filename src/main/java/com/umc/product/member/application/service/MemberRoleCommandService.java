package com.umc.product.member.application.service;

import com.umc.product.common.domain.enums.MemberRoleType;
import com.umc.product.member.application.port.in.command.ManageMemberRoleUseCase;
import com.umc.product.member.application.port.in.command.dto.UpdateMemberRoleCommand;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberRoleCommandService implements ManageMemberRoleUseCase {

    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;

    @Override
    public void updateRole(UpdateMemberRoleCommand command) {
        Member requester = getMember(command.requesterMemberId());
        if (!requester.isAdmin()) {
            throw new MemberDomainException(MemberErrorCode.MEMBER_ROLE_ACCESS_DENIED);
        }

        Member target = getMember(command.targetMemberId());
        validateDemotion(requester, target, command.roleType());

        target.changeRole(command.roleType());
        saveMemberPort.save(target);
    }

    private void validateDemotion(Member requester, Member target, MemberRoleType newRoleType) {
        if (newRoleType == null || newRoleType.isAdmin()) {
            return;
        }

        if (Objects.equals(requester.getId(), target.getId())) {
            throw new MemberDomainException(MemberErrorCode.MEMBER_ROLE_SELF_DEMOTION_DENIED);
        }

        if (target.isAdmin() && loadMemberPort.countByRoleType(MemberRoleType.ADMIN) <= 1) {
            throw new MemberDomainException(MemberErrorCode.LAST_ADMIN_ROLE_CHANGE_DENIED);
        }
    }

    private Member getMember(Long memberId) {
        return loadMemberPort.findById(memberId)
            .orElseThrow(() -> new MemberDomainException(MemberErrorCode.MEMBER_NOT_FOUND));
    }
}
