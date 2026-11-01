package com.umc.product.organization.adapter.in.web.dto.request;

import java.time.Instant;

import com.umc.product.organization.application.port.in.command.dto.UpdateGisuCommand;

import jakarta.validation.constraints.NotNull;

public record UpdateGisuRequest(@NotNull Instant startAt, @NotNull Instant endAt) {
    public UpdateGisuCommand toCommand(Long gisuId) {
        return new UpdateGisuCommand(gisuId, startAt, endAt);
    }
}
