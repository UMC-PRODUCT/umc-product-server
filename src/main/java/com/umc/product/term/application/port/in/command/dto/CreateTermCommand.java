package com.umc.product.term.application.port.in.command.dto;

import com.umc.product.term.domain.enums.TermType;
import lombok.Builder;

@Builder
public record CreateTermCommand(
    String link,
    boolean required,
    TermType type
) {
}
