package com.umc.product.recruitment.application.port.in.command.dto;

public record ResetDraftFormResponseCommand(
    Long recruitmentId,
    Long memberId
) {
}
