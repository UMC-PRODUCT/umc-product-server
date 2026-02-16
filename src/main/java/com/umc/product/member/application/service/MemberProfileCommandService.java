package com.umc.product.member.application.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberProfileCommandService implements ManageMemberProfileUseCase {

    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final SaveMemberProfilePort saveMemberProfilePort;

    @Override
    public void upsert(UpsertMemberProfileCommand command) {
        Member member = loadMemberPort.findById(command.memberId())
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

    @Override
    public void delete(Long memberId) {
        Member member = loadMemberPort.findById(memberId)
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