package com.umc.product.challenger.adapter.in.web.dto.request;

import com.umc.product.common.domain.enums.ChallengerPart;

public record CreateChallengerInfoRequest(
        Long memberId,
        ChallengerPart part,
        Long gisuId
) {
}
