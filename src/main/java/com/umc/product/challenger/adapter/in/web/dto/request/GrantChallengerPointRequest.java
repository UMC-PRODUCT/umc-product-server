package com.umc.product.challenger.adapter.in.web.dto.request;

import com.umc.product.challenger.domain.enums.PointType;

public record GrantChallengerPointRequest(
        PointType pointType,
        String description
) {

}
