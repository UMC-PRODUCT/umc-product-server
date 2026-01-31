package com.umc.product.member.application.port.in.query;

import com.umc.product.common.domain.enums.MemberStatus;

public record MemberProfileInfo(
    Long id,
    String name,
    String nickname,
    String email,
    Long schoolId,
    String schoolName,
    String profileImageLink,
    MemberStatus status
) {
    public static MemberProfileInfo from(MemberInfo memberInfo, String schoolName, String profileImageLink) {
        return new MemberProfileInfo(
            memberInfo.id(),
            memberInfo.name(),
            memberInfo.nickname(),
            memberInfo.email(),
            memberInfo.schoolId(),
            schoolName,
            profileImageLink,
            memberInfo.status()
        );
    }
}
