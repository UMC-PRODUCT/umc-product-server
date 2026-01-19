package com.umc.product.terms.application.port.in.command.dto;

import com.umc.product.terms.domain.enums.TermsType;
import lombok.Builder;

@Builder
public record CreateTermCommand(
        String title,
        String content,
        String version,
        boolean required,
        TermsType type
) {
}
