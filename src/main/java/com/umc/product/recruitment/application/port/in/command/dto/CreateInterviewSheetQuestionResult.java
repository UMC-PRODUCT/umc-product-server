package com.umc.product.recruitment.application.port.in.command.dto;

public record CreateInterviewSheetQuestionResult(
    Long questionId,
    Integer orderNo,
    String questionText
) {
}
