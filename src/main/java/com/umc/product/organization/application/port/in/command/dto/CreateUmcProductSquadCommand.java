package com.umc.product.organization.application.port.in.command.dto;

import java.time.Instant;

public record CreateUmcProductSquadCommand(
    Long requesterMemberId,
    String code,
    String name,
    String description,
    Instant startAt,
    Instant endAt,
    int sortOrder,
    boolean active
) {
    public static CreateUmcProductSquadCommand of(
        Long requesterMemberId,
        String code,
        String name,
        String description,
        Instant startAt,
        Instant endAt,
        int sortOrder,
        boolean active
    ) {
        return new CreateUmcProductSquadCommand(
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
