package com.umc.product.curriculum.adapter.in.web.v2.dto.request;

import com.umc.product.curriculum.domain.enums.FeedbackResult;

public record CreateMissionFeedbackRequest(
    Long missionSubmissionId,
    String content,
    FeedbackResult result
) {
}
