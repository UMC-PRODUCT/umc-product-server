package com.umc.product.command.organization.adapter.in.dto.request;

import com.umc.product.command.organization.application.port.in.dto.request.UpdateSchoolUseCaseRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateSchoolRequest(
        @NotNull Long schoolId,
        @NotBlank String schoolName,
        @NotNull String chapterId,
        String remark) {
    public UpdateSchoolUseCaseRequest toUseCaseRequest() {
        return new UpdateSchoolUseCaseRequest(
                schoolId,
                schoolName,
                chapterId,
                remark);
    }
}
