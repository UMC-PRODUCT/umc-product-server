package com.umc.product.organization.application.port.in.command.dto;

import java.time.Instant;

public record UpdateUmcProductGenerationCommand(
    Long umcProductGenerationId,
    Long requesterMemberId,
    Long generation,
    Instant startAt,
    Instant endAt,
    Boolean active
) {
    public static UpdateUmcProductGenerationCommand of(
        Long umcProductGenerationId,
        Long requesterMemberId,
        Long generation,
        Instant startAt,
        Instant endAt,
        Boolean active
    ) {
        return new UpdateUmcProductGenerationCommand(
            umcProductGenerationId,
            requesterMemberId,
            generation,
            startAt,
            endAt,
            active
        );
    }
}
