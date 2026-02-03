package com.umc.product.recruitment.application.port.in.command.dto;

public record DeleteLiveQuestionCommand(
        Long recruitmentId,
        Long assignmentId,
        Long liveQuestionId,
        Long memberId
) {
}
