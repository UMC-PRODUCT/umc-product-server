package com.umc.product.challenger.application.port.in.query.dto;

public record GlobalSearchChallengerItemInfo(
        Long challengerId,
        String nickname,
        String name,
        String schoolName,
        Long generation,
        String profileImageLink
) {
}
