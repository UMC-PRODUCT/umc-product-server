package com.umc.product.authorization.adapter.in.web.dto.response;

import lombok.Builder;

@Builder
public record CreateChallengerRoleResponse(
    Long challengerRoleId
) {
}
