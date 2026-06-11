package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.CreateProductTeamSquadCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CreateProductTeamSquadRequest(
    @NotNull @Size(max = 64) String code,
    @NotNull @Size(max = 100) String name,
    @Size(max = 1000) String description,
    Instant startAt,
    Instant endAt,
    Integer sortOrder,
    Boolean active
) {
    public CreateProductTeamSquadCommand toCommand(Long requesterMemberId) {
        return CreateProductTeamSquadCommand.of(
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
