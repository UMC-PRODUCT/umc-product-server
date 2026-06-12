package com.umc.product.organization.adapter.in.web.dto.request;

import java.time.Instant;

import com.umc.product.organization.application.port.in.command.dto.UpdateUmcProductSquadCommand;

import jakarta.validation.constraints.Size;

public record UpdateUmcProductSquadRequest(
    @Size(max = 64) String code,
    @Size(max = 100) String name,
    @Size(max = 1000) String description,
    Instant startAt,
    Instant endAt,
    Integer sortOrder,
    Boolean active
) {
    public UpdateUmcProductSquadCommand toCommand(Long squadId, Long requesterMemberId) {
        return UpdateUmcProductSquadCommand.of(
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
