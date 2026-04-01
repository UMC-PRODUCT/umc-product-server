package com.umc.product.curriculum.application.port.in.query.dto;

import com.umc.product.curriculum.domain.enums.WorkbookStatus;
import java.util.List;
import lombok.Builder;

@Builder
public record WorkbookSubmissionDetailInfo(
        Long challengerWorkbookId,
        WorkbookStatus status,
        String content,
        List<ReviewInfo> reviews
) {

    @Builder
    public record ReviewInfo(
            Long reviewId,
            Long reviewerChallengerId,
            String feedback,
            String bestReason
    ) {
    }
}
