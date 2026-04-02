package com.umc.product.curriculum.adapter.in.web.v1.dto.request;

import com.umc.product.curriculum.application.port.in.command.dto.UpdateReviewCommand;
import com.umc.product.curriculum.domain.enums.WorkbookStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "리뷰 수정 요청")
public record UpdateReviewRequest(
    @NotNull(message = "심사 결과는 필수입니다")
    @Schema(description = "심사 결과 (PASS 또는 FAIL)", example = "PASS")
    WorkbookStatus status,

    @Schema(description = "피드백", example = "잘 작성하셨습니다!")
    String feedback
) {
    public UpdateReviewCommand toCommand(Long reviewId, Long memberId) {
        return new UpdateReviewCommand(reviewId, memberId, status, feedback);
    }
}
