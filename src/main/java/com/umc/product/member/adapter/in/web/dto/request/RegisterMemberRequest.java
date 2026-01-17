package com.umc.product.member.adapter.in.web.dto.request;

public record RegisterMemberRequest(
        String oAuthVerificationToken,
        String name,
        String nickname,
        String emailVerificationToken,
        String schoolId,
        String profileImageId
) {
}
