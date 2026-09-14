package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.UnassignSchoolCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "학교 지부 배정 해제 요청")
public record UnassignSchoolRequest(
        @Schema(description = "기수 ID", example = "1")
        @NotNull(message = "기수 ID는 필수입니다")
        Long gisuId
) {
    public UnassignSchoolCommand toCommand(Long schoolId) {
        return new UnassignSchoolCommand(schoolId, gisuId);
    }
}
