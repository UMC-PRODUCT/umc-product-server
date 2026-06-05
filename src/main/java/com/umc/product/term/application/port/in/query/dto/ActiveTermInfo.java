package com.umc.product.term.application.port.in.query.dto;

import java.time.Instant;

import com.umc.product.term.domain.Term;
import com.umc.product.term.domain.enums.TermType;

public record ActiveTermInfo(
    Long id,
    TermType type,
    String typeDescription,
    String link,
    boolean isMandatory,
    Long version,
    Instant createdAt,
    Instant updatedAt
) {

    public static ActiveTermInfo from(Term term) {
        return new ActiveTermInfo(
            term.getId(),
            term.getType(),
            term.getType().getDescription(),
            term.getLink(),
            term.isRequired(),
            term.getId(),
            term.getCreatedAt(),
            term.getUpdatedAt()
        );
    }
}
