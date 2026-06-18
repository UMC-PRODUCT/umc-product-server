package com.umc.product.feedback.adapter.in.web.dto.response;

public record UserFeedbackSubmitResponse(
    Long formResponseId
) {
    public static UserFeedbackSubmitResponse from(Long formResponseId) {
        return new UserFeedbackSubmitResponse(formResponseId);
    }
}
