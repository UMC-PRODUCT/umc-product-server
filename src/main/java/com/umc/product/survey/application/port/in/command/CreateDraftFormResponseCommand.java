package com.umc.product.survey.application.port.in.command;

public record CreateDraftFormResponseCommand(
        Long userId,
        Long formId
) {
}
