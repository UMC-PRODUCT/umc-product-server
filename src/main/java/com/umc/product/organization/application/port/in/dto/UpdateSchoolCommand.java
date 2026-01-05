package com.umc.product.organization.application.port.in.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateSchoolCommand(
        @NotNull
        Long schoolId,
        @NotBlank
        String schoolName,
        @NotNull
        String chapterId,
        String remark

) {
}
