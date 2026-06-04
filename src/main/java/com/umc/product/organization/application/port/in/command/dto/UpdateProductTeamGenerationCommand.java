package com.umc.product.organization.application.port.in.command.dto;

import java.time.Instant;

public record UpdateProductTeamGenerationCommand(
    Long productTeamGenerationId,
    Long requesterMemberId,
    Long generation,
    Instant startAt,
    Instant endAt,
    Boolean active
) {
    public static UpdateProductTeamGenerationCommand of(
        Long productTeamGenerationId,
        Long requesterMemberId,
        Long generation,
        Instant startAt,
        Instant endAt,
        Boolean active
    ) {
        return new UpdateProductTeamGenerationCommand(
            productTeamGenerationId,
            requesterMemberId,
            generation,
            startAt,
            endAt,
            active
        );
    }
}
