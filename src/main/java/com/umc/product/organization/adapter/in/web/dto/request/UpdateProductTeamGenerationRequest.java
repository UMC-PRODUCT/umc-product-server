package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.UpdateProductTeamGenerationCommand;
import java.time.Instant;

public record UpdateProductTeamGenerationRequest(
    Long generation,
    Instant startAt,
    Instant endAt,
    Boolean active
) {
    public UpdateProductTeamGenerationCommand toCommand(Long productTeamGenerationId, Long requesterMemberId) {
        return UpdateProductTeamGenerationCommand.of(
            productTeamGenerationId,
            requesterMemberId,
            generation,
            startAt,
            endAt,
            active
        );
    }
}
