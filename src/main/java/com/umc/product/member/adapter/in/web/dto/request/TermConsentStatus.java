package com.umc.product.member.adapter.in.web.dto.request;

public record TermConsentStatus(
        Long termsId,
        boolean isAgreed
) {
}
