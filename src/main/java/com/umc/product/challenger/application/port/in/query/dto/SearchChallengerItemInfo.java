package com.umc.product.challenger.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;

public record SearchChallengerItemInfo(
        Long challengerId,
        Long memberId,
        Long gisuId,
        ChallengerPart part,
        String name,
        String nickname,
        Double pointSum,
        String profileImageLink
) {
}
