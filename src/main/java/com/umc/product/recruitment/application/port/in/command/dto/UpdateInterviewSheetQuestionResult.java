package com.umc.product.recruitment.application.port.in.command.dto;

public record UpdateInterviewSheetQuestionResult(
        Long questionId,
        String questionText
) {
}
