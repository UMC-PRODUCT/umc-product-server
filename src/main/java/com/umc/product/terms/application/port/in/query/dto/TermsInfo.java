package com.umc.product.terms.application.port.in.query.dto;

import com.umc.product.terms.domain.enums.TermsType;
import java.time.Instant;

public record TermsInfo(
        Long id,
        String title,
        String content,
        boolean isMandatory,
        TermsType type,
        Instant effectiveDate
) {
}
