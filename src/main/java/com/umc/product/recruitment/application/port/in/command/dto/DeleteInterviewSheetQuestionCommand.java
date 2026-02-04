package com.umc.product.recruitment.application.port.in.command.dto;

public record DeleteInterviewSheetQuestionCommand(
        Long recruitmentId, Long questionId, Long requesterMemberId
) {
}
