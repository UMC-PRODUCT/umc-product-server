package com.umc.product.term.adapter.in.web.dto.response;

import java.util.List;

import com.umc.product.term.application.port.in.query.dto.RequiredTermConsentStatusInfo;

public record RequiredTermConsentStatusResponse(
    boolean needsReconsent,
    List<TermResponse> missingRequiredTerms
) {

    public static RequiredTermConsentStatusResponse from(RequiredTermConsentStatusInfo info) {
        return new RequiredTermConsentStatusResponse(
            info.needsReconsent(),
            info.missingRequiredTerms().stream()
                .map(TermResponse::from)
                .toList()
        );
    }
}
