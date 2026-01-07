package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.CreateSchoolCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateSchoolRequest(
        @NotBlank String schoolName,
        @NotBlank String chapterId,
        @NotNull String remark
) {
    public CreateSchoolCommand toCommand() {
        return new CreateSchoolCommand(schoolName, chapterId, remark);
    }
}
