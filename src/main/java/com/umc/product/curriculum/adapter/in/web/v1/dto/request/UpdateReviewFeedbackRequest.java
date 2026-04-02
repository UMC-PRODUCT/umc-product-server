package com.umc.product.curriculum.adapter.in.web.v1.dto.request;

import com.umc.product.curriculum.application.port.in.command.dto.UpdateReviewFeedbackCommand;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리뷰 피드백 수정 요청")
public record UpdateReviewFeedbackRequest(
    @Schema(description = "수정할 피드백", example = "깃허브 링크에 README가 빠져있습니다.")
    String feedback
) {
    public UpdateReviewFeedbackCommand toCommand(Long reviewId, Long memberId) {
        return new UpdateReviewFeedbackCommand(reviewId, memberId, feedback);
    }
}
