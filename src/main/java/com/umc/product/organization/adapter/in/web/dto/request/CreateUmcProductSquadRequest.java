package com.umc.product.organization.adapter.in.web.dto.request;

import java.time.Instant;

import com.umc.product.organization.application.port.in.command.dto.CreateUmcProductSquadCommand;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUmcProductSquadRequest(
    @NotNull @Size(max = 64) String code,
    @NotNull @Size(max = 100) String name,
    @Size(max = 1000) String description,
    Instant startAt,
    Instant endAt,
    Integer sortOrder,
    Boolean active
) {
    public CreateUmcProductSquadCommand toCommand(Long requesterMemberId) {
        return CreateUmcProductSquadCommand.of(
            requesterMemberId,
            code,
            name,
            description,
            startAt,
            endAt,
            sortOrder == null ? 0 : sortOrder,
            !Boolean.FALSE.equals(active)
        );
    }
}
