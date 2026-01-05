package com.umc.product.command.organization.application.port.in.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateSchoolUseCaseRequest(
        @NotBlank
        String schoolName,
        Long chapterId,
        String remark
) {
}

