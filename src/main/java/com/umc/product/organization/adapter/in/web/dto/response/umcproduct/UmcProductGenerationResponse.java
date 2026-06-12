package com.umc.product.organization.adapter.in.web.dto.response.umcproduct;

import java.time.Instant;

import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductGenerationInfo;

public record UmcProductGenerationResponse(
    Long umcProductGenerationId,
    Long generation,
    Instant startAt,
    Instant endAt,
    boolean active
) {
    public static UmcProductGenerationResponse from(UmcProductGenerationInfo info) {
        return new UmcProductGenerationResponse(
            info.umcProductGenerationId(),
            info.generation(),
            info.startAt(),
            info.endAt(),
            info.active()
        );
    }
}
