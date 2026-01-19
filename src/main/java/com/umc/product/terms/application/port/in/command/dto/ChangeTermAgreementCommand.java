package com.umc.product.terms.application.port.in.command.dto;

public record ChangeTermAgreementCommand(
        Long memberId,
        Long termId,
        boolean isAgreed
) {
}
