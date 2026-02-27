package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.command.dto.CreateInterviewSheetQuestionResult;

public record CreateInterviewSheetQuestionResponse(
    Long questionId,
    Integer orderNo,
    String questionText
) {
    public static CreateInterviewSheetQuestionResponse from(CreateInterviewSheetQuestionResult result) {
        return new CreateInterviewSheetQuestionResponse(result.questionId(), result.orderNo(), result.questionText());
    }
}
