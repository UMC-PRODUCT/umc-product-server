package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.UpdateProductTeamSquadCommand;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record UpdateProductTeamSquadRequest(
    @Size(max = 64) String code,
    @Size(max = 100) String name,
    @Size(max = 1000) String description,
    Instant startAt,
    Instant endAt,
    Integer sortOrder,
    Boolean active
) {
    public UpdateProductTeamSquadCommand toCommand(Long squadId, Long requesterMemberId) {
        return UpdateProductTeamSquadCommand.of(
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
