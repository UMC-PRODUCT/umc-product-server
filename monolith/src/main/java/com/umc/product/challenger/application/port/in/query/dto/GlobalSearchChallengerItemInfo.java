package com.umc.product.challenger.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;

public record GlobalSearchChallengerItemInfo(
        Long memberId,
        String nickname,
        String name,
        String schoolName,
        Long gisu,
        ChallengerPart part,
        String profileImageLink
) {
}
