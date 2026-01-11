package com.umc.product.survey.application.port.in.command;

public record SubmitFormResponseCommand(
        Long userId,
        Long formResponseId
) {
}
