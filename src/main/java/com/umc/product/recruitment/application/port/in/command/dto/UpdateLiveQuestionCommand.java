package com.umc.product.recruitment.application.port.in.command.dto;

public record UpdateLiveQuestionCommand(
    Long recruitmentId,
    Long assignmentId,
    Long liveQuestionId,
    Long memberId,
    String text
) {
}
