package com.umc.product.member.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.audit.application.port.in.annotation.Audited;
import com.umc.product.audit.domain.AuditAction;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.member.application.port.in.command.ManageMemberProfileUseCase;
import com.umc.product.member.application.port.in.command.dto.UpsertMemberProfileCommand;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.application.port.out.SaveMemberProfilePort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.MemberProfile;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberProfileCommandService implements ManageMemberProfileUseCase {

    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final SaveMemberProfilePort saveMemberProfilePort;

    @Audited(
        domain = Domain.MEMBER,
        action = AuditAction.UPDATE,
        targetType = "MemberProfile",
        targetId = "#command.memberId()",
        description = "'회원 프로필을 저장했습니다.'"
    )
    @Override
    public void upsert(UpsertMemberProfileCommand command) {
        Member member = loadMemberPort.findByIdForUpdate(command.memberId())
            .orElseThrow(() -> new MemberDomainException(MemberErrorCode.MEMBER_NOT_FOUND));

        MemberProfile profile = member.getProfile();

        if (profile == null) {
            profile = MemberProfile.fromLinks(command.links());
            saveMemberProfilePort.save(profile);
            member.assignProfile(profile);
            saveMemberPort.save(member);
        } else {
            profile.updateLinks(command.links());
        }
    }

    @Audited(
        domain = Domain.MEMBER,
        action = AuditAction.DELETE,
        targetType = "MemberProfile",
        targetId = "#memberId",
        description = "'회원 프로필을 삭제했습니다.'"
    )
    @Override
    public void delete(Long memberId) {
        Member member = loadMemberPort.findByIdForUpdate(memberId)
            .orElseThrow(() -> new MemberDomainException(MemberErrorCode.MEMBER_NOT_FOUND));

        MemberProfile profile = member.getProfile();
        if (profile == null) {
            throw new MemberDomainException(MemberErrorCode.MEMBER_PROFILE_NOT_FOUND);
        }

        member.removeProfile();
        saveMemberPort.save(member);
        saveMemberProfilePort.delete(profile);
    }
}
