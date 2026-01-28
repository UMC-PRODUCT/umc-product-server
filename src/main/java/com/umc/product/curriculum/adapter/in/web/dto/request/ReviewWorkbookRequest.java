package com.umc.product.curriculum.adapter.in.web.dto.request;

import com.umc.product.curriculum.application.port.in.command.ReviewWorkbookCommand;
import com.umc.product.curriculum.domain.enums.WorkbookStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "워크북 검토 요청")
public record ReviewWorkbookRequest(
        @Schema(description = "검토 결과 (PASS: 통과, FAIL: 반려)", example = "PASS")
        @NotNull(message = "검토 결과는 필수입니다")
        WorkbookStatus status,

        @Schema(description = "피드백", example = "잘 작성하셨습니다!")
        String feedback
) {
    public ReviewWorkbookCommand toCommand(Long challengerWorkbookId) {
        return new ReviewWorkbookCommand(challengerWorkbookId, status, feedback);
    }
}
