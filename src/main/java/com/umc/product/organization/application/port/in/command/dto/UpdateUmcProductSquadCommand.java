package com.umc.product.organization.application.port.in.command.dto;

import java.time.Instant;

public record UpdateUmcProductSquadCommand(
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
    public static UpdateUmcProductSquadCommand of(
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
        return new UpdateUmcProductSquadCommand(
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
