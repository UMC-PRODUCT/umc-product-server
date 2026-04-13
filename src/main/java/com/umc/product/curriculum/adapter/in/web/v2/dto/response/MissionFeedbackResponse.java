package com.umc.product.curriculum.adapter.in.web.v2.dto.response;

import com.umc.product.curriculum.domain.enums.FeedbackResult;
import lombok.Builder;

@Builder
public record MissionFeedbackResponse(
    Long missionFeedbackId,
    Long reviewerMemberId,
    String content,
    FeedbackResult feedbackResult
) {
}
