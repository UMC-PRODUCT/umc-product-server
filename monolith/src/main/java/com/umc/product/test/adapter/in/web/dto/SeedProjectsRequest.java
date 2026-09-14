package com.umc.product.test.adapter.in.web.dto;

import com.umc.product.test.application.port.in.command.dto.SeedProjectsCommand;
import jakarta.validation.constraints.Positive;

public record SeedProjectsRequest(
    @Positive
    int projectCount,
    Long gisuId
) {

    public SeedProjectsCommand toCommand() {
        return new SeedProjectsCommand(projectCount, gisuId);
    }
}
