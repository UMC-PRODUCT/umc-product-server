package com.umc.product.terms.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TermsConsentStatus {
    AGREED("동의"),
    WITHDRAWN("철회");

    private final String description;
}
