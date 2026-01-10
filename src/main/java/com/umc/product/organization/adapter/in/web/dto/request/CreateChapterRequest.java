package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.CreateChapterCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateChapterRequest(@NotNull Long gisuId, @NotBlank String name) {
    public CreateChapterCommand toCommand() {
        return new CreateChapterCommand(gisuId, name);
    }
}
