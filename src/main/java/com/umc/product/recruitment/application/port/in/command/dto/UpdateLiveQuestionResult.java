package com.umc.product.recruitment.application.port.in.command.dto;

public record UpdateLiveQuestionResult(
        Long liveQuestionId,
        String text
) {
}
