package com.umc.product.organization.application.port.in.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterSchoolCommand(
        @NotBlank
        String schoolName,
        Long chapterId,
        String remark
) {
}
