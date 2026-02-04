package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.command.dto.UpdateInterviewSheetQuestionResult;

public record UpdateInterviewSheetQuestionResponse(
        Long questionId,
        String questionText
) {
    public static UpdateInterviewSheetQuestionResponse from(UpdateInterviewSheetQuestionResult result) {
        return new UpdateInterviewSheetQuestionResponse(result.questionId(), result.questionText());
    }
}