package com.umc.product.organization.application.port.in.query.dto;

public record MemberInfo(
        Long challengerId,
        Long memberId,
        String name,
        String profileImageUrl
) {
}
