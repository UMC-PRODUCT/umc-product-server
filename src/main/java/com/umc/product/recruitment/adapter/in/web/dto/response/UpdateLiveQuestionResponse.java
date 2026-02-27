package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.command.dto.UpdateLiveQuestionResult;

public record UpdateLiveQuestionResponse(
    Long liveQuestionId,
    String text
) {
    public static UpdateLiveQuestionResponse from(UpdateLiveQuestionResult r) {
        return new UpdateLiveQuestionResponse(r.liveQuestionId(), r.text());
    }
}
