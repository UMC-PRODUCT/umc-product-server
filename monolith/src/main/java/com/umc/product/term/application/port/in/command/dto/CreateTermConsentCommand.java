package com.umc.product.term.application.port.in.command.dto;

import lombok.Builder;

@Builder
public record CreateTermConsentCommand(
    Long memberId,
    Long termId,
    boolean isAgreed
) {
}
