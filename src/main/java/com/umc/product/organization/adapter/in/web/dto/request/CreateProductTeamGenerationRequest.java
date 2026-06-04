package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.CreateProductTeamGenerationCommand;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record CreateProductTeamGenerationRequest(
    @NotNull Long generation,
    @NotNull Instant startAt,
    @NotNull Instant endAt,
    Boolean active
) {
    public CreateProductTeamGenerationCommand toCommand(Long requesterMemberId) {
        return CreateProductTeamGenerationCommand.of(
            requesterMemberId,
            generation,
            startAt,
            endAt,
            Boolean.TRUE.equals(active)
        );
    }
}
