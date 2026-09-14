package com.umc.product.term.application.port.in.query.dto;

import com.umc.product.term.domain.Term;
import com.umc.product.term.domain.enums.TermType;

public record TermInfo(
    Long id,
    String link,
    boolean isMandatory,
    TermType type
) {
    public static TermInfo from(Term term) {
        return new TermInfo(
            term.getId(),
            term.getLink(),
            term.isRequired(),
            term.getType()
        );
    }
}
