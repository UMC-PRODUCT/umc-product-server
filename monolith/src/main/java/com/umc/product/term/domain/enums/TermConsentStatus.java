package com.umc.product.term.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TermConsentStatus {
    AGREED("동의"),
    WITHDRAWN("철회");

    private final String description;
}
