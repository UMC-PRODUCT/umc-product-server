package com.umc.product.organization.application.port.in.dto;

import com.umc.product.organization.domain.Chapter;
import jakarta.validation.constraints.NotBlank;

public record RegisterSchoolCommand(
        @NotBlank
        String schoolName,
        Long chapterId,
        String remark
) {
}
