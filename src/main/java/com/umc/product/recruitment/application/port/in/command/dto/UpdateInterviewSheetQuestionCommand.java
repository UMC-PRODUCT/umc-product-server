package com.umc.product.recruitment.application.port.in.command.dto;

public record UpdateInterviewSheetQuestionCommand(
    Long recruitmentId,
    Long questionId,
    String questionText,
    Long requesterMemberId
) {
}
