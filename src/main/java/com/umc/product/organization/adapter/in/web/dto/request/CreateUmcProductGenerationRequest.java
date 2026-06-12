package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.CreateUmcProductGenerationCommand;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record CreateUmcProductGenerationRequest(
    @NotNull Long generation,
    @NotNull Instant startAt,
    @NotNull Instant endAt,
    Boolean active
) {
    public CreateUmcProductGenerationCommand toCommand(Long requesterMemberId) {
        return CreateUmcProductGenerationCommand.of(
            requesterMemberId,
            generation,
            startAt,
            endAt,
            Boolean.TRUE.equals(active)
        );
    }
}
