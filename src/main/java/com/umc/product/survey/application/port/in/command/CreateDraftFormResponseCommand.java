package com.umc.product.survey.application.port.in.command;

public record CreateDraftFormResponseCommand(
        Long memberId,
        Long formId
) {
}
