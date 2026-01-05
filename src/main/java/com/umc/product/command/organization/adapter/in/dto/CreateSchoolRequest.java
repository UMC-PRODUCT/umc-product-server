package com.umc.product.command.organization.adapter.in.dto;

import com.umc.product.command.organization.application.port.in.dto.request.CreateSchoolUseCaseRequest;
import jakarta.validation.constraints.NotBlank;

public record CreateSchoolRequest(
        @NotBlank String schoolName,
        Long chapterId,
        String remark) {
    public CreateSchoolUseCaseRequest toUseCaseRequest() {
        return new CreateSchoolUseCaseRequest(
                schoolName,
                chapterId,
                remark);
    }
}

