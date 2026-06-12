package com.umc.product.organization.application.port.in.command.dto;

import java.time.Instant;

public record CreateUmcProductGenerationCommand(
    Long requesterMemberId,
    Long generation,
    Instant startAt,
    Instant endAt,
    boolean active
) {
    public static CreateUmcProductGenerationCommand of(
        Long requesterMemberId,
        Long generation,
        Instant startAt,
        Instant endAt,
        boolean active
    ) {
        return new CreateUmcProductGenerationCommand(requesterMemberId, generation, startAt, endAt, active);
    }
}
