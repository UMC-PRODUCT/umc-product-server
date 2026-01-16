package com.umc.product.member.application.port.in.query;

import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.member.domain.Member;

public record MemberInfo(
        Long id,
        String name,
        String nickname,
        String email,
        Long schoolId,
        Long profileImageId,
        MemberStatus status
) {
    public static MemberInfo from(Member member) {
        return new MemberInfo(
                member.getId(),
                member.getName(),
                member.getNickname(),
                member.getEmail(),
                member.getSchoolId(),
                member.getProfileImageId(),
                member.getStatus()
        );
    }
}
