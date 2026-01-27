package com.umc.product.recruitment.application.port.in.command.dto;

public record CreateDraftFormResponseCommand(
        Long recruitmentId,
        Long memberId
) {
}
