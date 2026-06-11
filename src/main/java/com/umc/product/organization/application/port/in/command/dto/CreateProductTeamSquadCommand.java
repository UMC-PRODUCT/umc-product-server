package com.umc.product.organization.application.port.in.command.dto;

import java.time.Instant;

public record CreateProductTeamSquadCommand(
    Long requesterMemberId,
    String code,
    String name,
    String description,
    Instant startAt,
    Instant endAt,
    int sortOrder,
    boolean active
) {
    public static CreateProductTeamSquadCommand of(
        Long requesterMemberId,
        String code,
        String name,
        String description,
        Instant startAt,
        Instant endAt,
        int sortOrder,
        boolean active
    ) {
        return new CreateProductTeamSquadCommand(
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
