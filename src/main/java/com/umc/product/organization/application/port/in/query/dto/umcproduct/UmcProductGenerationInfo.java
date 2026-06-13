package com.umc.product.organization.application.port.in.query.dto.umcproduct;

import java.time.Instant;

import com.umc.product.organization.domain.UmcProductGeneration;

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
