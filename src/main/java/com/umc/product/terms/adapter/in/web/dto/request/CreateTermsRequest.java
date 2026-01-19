package com.umc.product.terms.adapter.in.web.dto.request;

import com.umc.product.terms.domain.enums.TermsType;
import java.time.Instant;

public record CreateTermsRequest(
        String title,
        String content,
        String version,
        boolean isMandatory,
        TermsType termsType,
        Instant effectiveDate
) {
}
