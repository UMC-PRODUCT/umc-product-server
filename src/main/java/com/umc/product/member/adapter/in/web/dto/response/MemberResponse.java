package com.umc.product.member.adapter.in.web.dto.response;

import com.umc.product.member.application.port.in.query.MemberInfo;
import com.umc.product.member.domain.enums.MemberStatus;

public record MemberResponse(
        Long id,
        String name,
        String nickname,
        String email,
        Long schoolId,
        Long profileImageId,
        MemberStatus status
) {
    public static MemberResponse from(MemberInfo memberInfo) {
        return new MemberResponse(
                memberInfo.id(),
                memberInfo.name(),
                memberInfo.nickname(),
                memberInfo.email(),
                memberInfo.schoolId(),
                memberInfo.profileImageId(),
                memberInfo.status()
        );
    }
}
