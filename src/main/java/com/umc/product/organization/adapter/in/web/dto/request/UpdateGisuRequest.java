package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.UpdateGisuCommand;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UpdateGisuRequest(@NotNull LocalDate startAt, @NotNull LocalDate endAt) {
    public UpdateGisuCommand toCommand(Long gisuId) {
        return new UpdateGisuCommand(gisuId, startAt, endAt);
    }
}