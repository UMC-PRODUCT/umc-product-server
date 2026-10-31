package com.umc.product.member.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.audit.application.port.in.annotation.Audited;
import com.umc.product.audit.domain.AuditAction;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.member.application.port.in.command.EditBusinessCardUseCase;
import com.umc.product.member.application.port.in.command.dto.EditBusinessCardCommand;
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
public class BusinessCardCommandService implements EditBusinessCardUseCase {

    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final SaveMemberProfilePort saveMemberProfilePort;
    private final MemberRegistrationValidator registrationValidator;

    @Audited(
        domain = Domain.MEMBER,
        action = AuditAction.UPDATE,
        targetType = "MemberProfile",
        targetId = "#command.memberId()",
        description = "'명함을 수정했습니다.'"
    )
    @Override
    public void edit(EditBusinessCardCommand command) {
        Member member = loadMemberPort.findByIdForUpdate(command.memberId())
            .orElseThrow(() -> new MemberDomainException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (command.profileImageId() != null) {
            registrationValidator.validateProfileImageExists(command.profileImageId());
            member.updateProfile(null, command.profileImageId());
        }

        MemberProfile profile = member.getProfile();
        boolean isNewProfile = profile == null;
        if (isNewProfile) {
            profile = MemberProfile.fromLinks(command.linkTypeAndLinks());
        } else {
            profile.updateLinks(command.linkTypeAndLinks());
        }
        profile.updateContactEmail(command.contact().email());
        profile.changeVisibility(command.contact().isPublic(), command.linkVisibilities());

        if (isNewProfile) {
            saveMemberProfilePort.save(profile);
            member.assignProfile(profile);
            saveMemberPort.save(member);
        }
    }
}
