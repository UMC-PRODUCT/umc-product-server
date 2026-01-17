package com.umc.product.challenger.adapter.in.web.dto.request;

import com.umc.product.common.domain.enums.ChallengerPart;

public record EditChallengerRequest(
        ChallengerPart newPart
) {
}
