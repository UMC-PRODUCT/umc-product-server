package com.umc.product.organization.adapter.in.web.dto.response.productteam;

import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamGenerationInfo;
import java.time.Instant;

public record ProductTeamGenerationResponse(
    Long productTeamGenerationId,
    Long generation,
    Instant startAt,
    Instant endAt,
    boolean active
) {
    public static ProductTeamGenerationResponse from(ProductTeamGenerationInfo info) {
        return new ProductTeamGenerationResponse(
            info.productTeamGenerationId(),
            info.generation(),
            info.startAt(),
            info.endAt(),
            info.active()
        );
    }
}
