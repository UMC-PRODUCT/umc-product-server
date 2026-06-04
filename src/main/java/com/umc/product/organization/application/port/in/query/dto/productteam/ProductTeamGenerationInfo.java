package com.umc.product.organization.application.port.in.query.dto.productteam;

import com.umc.product.organization.domain.ProductTeamGeneration;
import java.time.Instant;

public record ProductTeamGenerationInfo(
    Long productTeamGenerationId,
    Long generation,
    Instant startAt,
    Instant endAt,
    boolean active
) {
    public static ProductTeamGenerationInfo from(ProductTeamGeneration generation) {
        return new ProductTeamGenerationInfo(
            generation.getId(),
            generation.getGeneration(),
            generation.getStartAt(),
            generation.getEndAt(),
            generation.isActive()
        );
    }
}
