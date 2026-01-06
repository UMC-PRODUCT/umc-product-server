package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.CreateSchoolCommand;
import jakarta.validation.constraints.NotBlank;

public record CreateSchoolRequest(
        @NotBlank String schoolName,
        Long chapterId,
        String remark
) {
    public CreateSchoolCommand toCommand() {
        return new CreateSchoolCommand(schoolName, chapterId, remark);
    }
}
