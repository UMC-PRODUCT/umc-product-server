package com.umc.product.organization.application.port.in.command.dto;

import java.time.Instant;

public record CreateProductTeamGenerationCommand(
    Long requesterMemberId,
    Long generation,
    Instant startAt,
    Instant endAt,
    boolean active
) {
    public static CreateProductTeamGenerationCommand of(
        Long requesterMemberId,
        Long generation,
        Instant startAt,
        Instant endAt,
        boolean active
    ) {
        return new CreateProductTeamGenerationCommand(requesterMemberId, generation, startAt, endAt, active);
    }
}
