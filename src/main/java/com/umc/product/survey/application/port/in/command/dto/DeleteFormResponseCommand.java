package com.umc.product.survey.application.port.in.command.dto;

public record DeleteFormResponseCommand(
        Long memberId,
        Long formResponseId
) {
}
