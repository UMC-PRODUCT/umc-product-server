package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.AssignSchoolCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "학교 지부 배정 요청")
public record AssignSchoolRequest(
        @Schema(description = "배정할 지부 ID", example = "1")
        @NotNull(message = "지부 ID는 필수입니다")
        Long chapterId
) {
    public AssignSchoolCommand toCommand(Long schoolId) {
        return new AssignSchoolCommand(schoolId, chapterId);
    }
}
