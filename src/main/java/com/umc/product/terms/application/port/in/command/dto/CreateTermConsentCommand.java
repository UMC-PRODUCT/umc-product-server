package com.umc.product.terms.application.port.in.command.dto;

import lombok.Builder;

@Builder
public record CreateTermConsentCommand(
        Long memberId,
        Long termId,
        boolean isAgreed
) {
}
