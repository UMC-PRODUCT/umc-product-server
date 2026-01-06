package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.UpdateSchoolCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateSchoolRequest(
        @NotNull(message = "학교 ID는 필수입니다")
        Long schoolId,
        @NotBlank String schoolName,
        @NotNull Long chapterId,
        String remark
) {
    public UpdateSchoolCommand toCommand() {
        return new UpdateSchoolCommand(schoolId, schoolName, chapterId, remark);
    }
}
