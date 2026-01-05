package com.umc.product.command.organization.application.port.in.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateSchoolUseCaseRequest(
        @NotNull
        Long schoolId,
        @NotBlank
        String schoolName,
        @NotNull
        String chapterId,
        String remark

) {
}

