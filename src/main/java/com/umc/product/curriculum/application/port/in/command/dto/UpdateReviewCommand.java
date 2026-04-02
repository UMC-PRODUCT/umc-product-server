package com.umc.product.curriculum.application.port.in.command.dto;

import com.umc.product.curriculum.domain.enums.WorkbookStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateReviewCommand(
        @NotNull(message = "리뷰 ID는 필수입니다")
        Long reviewId,
        @NotNull(message = "요청자 멤버 ID는 필수입니다")
        Long memberId,
        @NotNull(message = "심사 결과는 필수입니다")
        WorkbookStatus status,
        String feedback
) {
}
