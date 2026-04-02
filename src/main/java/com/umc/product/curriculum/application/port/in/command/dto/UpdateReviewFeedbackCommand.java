package com.umc.product.curriculum.application.port.in.command.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateReviewFeedbackCommand(
        @NotNull(message = "리뷰 ID는 필수입니다")
        Long reviewId,
        @NotNull(message = "요청자 멤버 ID는 필수입니다")
        Long memberId,
        String feedback
) {
}
