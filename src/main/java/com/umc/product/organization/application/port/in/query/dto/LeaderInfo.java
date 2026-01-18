package com.umc.product.organization.application.port.in.query.dto;

public record LeaderInfo(
        Long challengerId,
        String name,
        String profileImageUrl
) {
}
