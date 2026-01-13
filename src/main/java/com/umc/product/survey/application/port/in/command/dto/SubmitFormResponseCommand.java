package com.umc.product.survey.application.port.in.command.dto;

public record SubmitFormResponseCommand(
        Long memberId,
        Long formResponseId
) {
}
