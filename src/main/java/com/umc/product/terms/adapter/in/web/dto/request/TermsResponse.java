package com.umc.product.terms.adapter.in.web.dto.request;

import com.umc.product.terms.application.port.in.query.dto.TermsInfo;
import lombok.Builder;

@Builder
public record TermsResponse(
        Long id,
        String title,
        String content,
        boolean isMandatory
) {
    public static TermsResponse from(TermsInfo termsInfo) {
        return TermsResponse.builder()
                .id(termsInfo.id())
                .title(termsInfo.title())
                .content(termsInfo.content())
                .isMandatory(termsInfo.isMandatory())
                .build();
    }
}
