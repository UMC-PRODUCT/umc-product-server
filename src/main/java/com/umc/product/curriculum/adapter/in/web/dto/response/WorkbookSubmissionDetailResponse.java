package com.umc.product.curriculum.adapter.in.web.dto.response;

import com.umc.product.curriculum.application.port.in.query.dto.WorkbookSubmissionDetailInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "워크북 제출 상세 응답")
public record WorkbookSubmissionDetailResponse(
        @Schema(description = "챌린저 워크북 ID", example = "1")
        Long challengerWorkbookId,
        @Schema(description = "제출 URL", example = "https://github.com/user/repo")
        String submission
) {
    public static WorkbookSubmissionDetailResponse from(WorkbookSubmissionDetailInfo info) {
        return new WorkbookSubmissionDetailResponse(
                info.challengerWorkbookId(),
                info.submission()
        );
    }
}
