package com.umc.product.survey.application.port.in.command.dto;

public record CreateDraftFormResponseCommand(
        Long memberId,
        Long formId
) {
}
