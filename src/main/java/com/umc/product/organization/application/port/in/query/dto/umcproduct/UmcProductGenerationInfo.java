package com.umc.product.organization.application.port.in.query.dto.umcproduct;

import com.umc.product.organization.domain.UmcProductGeneration;
import java.time.Instant;

public record UmcProductGenerationInfo(
    Long umcProductGenerationId,
    Long generation,
    Instant startAt,
    Instant endAt,
    boolean active
) {
    public static UmcProductGenerationInfo from(UmcProductGeneration generation) {
        return new UmcProductGenerationInfo(
            generation.getId(),
            generation.getGeneration(),
            generation.getStartAt(),
            generation.getEndAt(),
            generation.isActive()
        );
    }
}
