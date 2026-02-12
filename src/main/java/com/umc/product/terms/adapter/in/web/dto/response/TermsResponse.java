package com.umc.product.terms.adapter.in.web.dto.response;

import com.umc.product.terms.application.port.in.query.dto.TermsInfo;
import lombok.Builder;

@Builder
public record TermsResponse(
    Long id,
    String link,
    boolean isMandatory
) {
    public static TermsResponse from(TermsInfo termsInfo) {
        return TermsResponse.builder()
            .id(termsInfo.id())
            .link(termsInfo.link())
            .isMandatory(termsInfo.isMandatory())
            .build();
    }
}
