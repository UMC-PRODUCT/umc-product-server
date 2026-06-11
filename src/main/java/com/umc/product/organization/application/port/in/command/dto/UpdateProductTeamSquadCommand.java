package com.umc.product.organization.application.port.in.command.dto;

import java.time.Instant;

public record UpdateProductTeamSquadCommand(
    Long squadId,
    Long requesterMemberId,
    String code,
    String name,
    String description,
    Instant startAt,
    Instant endAt,
    Integer sortOrder,
    Boolean active
) {
    public static UpdateProductTeamSquadCommand of(
        Long squadId,
        Long requesterMemberId,
        String code,
        String name,
        String description,
        Instant startAt,
        Instant endAt,
        Integer sortOrder,
        Boolean active
    ) {
        return new UpdateProductTeamSquadCommand(
            squadId,
            requesterMemberId,
            code,
            name,
            description,
            startAt,
            endAt,
            sortOrder,
            active
        );
    }
}
