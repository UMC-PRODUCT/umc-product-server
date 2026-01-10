package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.CreateGisuCommand;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateGisuRequest(@NotNull Long number, @NotNull LocalDate startAt, @NotNull LocalDate endAt) {
    public CreateGisuCommand toCommand() {
        return new CreateGisuCommand(number, startAt, endAt);
    }
}
