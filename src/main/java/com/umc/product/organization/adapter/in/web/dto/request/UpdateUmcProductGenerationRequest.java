package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.UpdateUmcProductGenerationCommand;
import java.time.Instant;

public record UpdateUmcProductGenerationRequest(
    Long generation,
    Instant startAt,
    Instant endAt,
    Boolean active
) {
    public UpdateUmcProductGenerationCommand toCommand(Long umcProductGenerationId, Long requesterMemberId) {
        return UpdateUmcProductGenerationCommand.of(
            umcProductGenerationId,
            requesterMemberId,
            generation,
            startAt,
            endAt,
            active
        );
    }
}
