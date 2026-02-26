package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.UpdateGisuCommand;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record UpdateGisuRequest(@NotNull Instant startAt, @NotNull Instant endAt) {
    public UpdateGisuCommand toCommand(Long gisuId) {
        return new UpdateGisuCommand(gisuId, startAt, endAt);
    }
}