package com.umc.product.survey.application.port.in.command.dto;

import lombok.Builder;

@Builder
public record CancelFormResponseCommand(
    Long formId,
    Long respondentMemberId
) {
}
