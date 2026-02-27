package com.umc.product.recruitment.application.port.in.command.dto;

public record CreateLiveQuestionCommand(
    Long recruitmentId,
    Long assignmentId,
    Long memberId,
    String text
) {
}
