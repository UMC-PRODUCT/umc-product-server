package com.umc.product.survey.application.port.in.command;

public record DeleteFormResponseCommand(
        Long memberId,
        Long formResponseId
) {
}
