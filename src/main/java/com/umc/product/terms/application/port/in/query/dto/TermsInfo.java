package com.umc.product.terms.application.port.in.query.dto;

import com.umc.product.terms.domain.Terms;
import com.umc.product.terms.domain.enums.TermsType;

public record TermsInfo(
    Long id,
    String link,
    boolean isMandatory,
    TermsType type
) {
    public static TermsInfo from(Terms term) {
        return new TermsInfo(
            term.getId(),
            term.getLink(),
            term.isRequired(),
            term.getType()
        );
    }
}
