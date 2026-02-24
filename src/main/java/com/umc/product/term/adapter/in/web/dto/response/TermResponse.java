package com.umc.product.term.adapter.in.web.dto.response;

import com.umc.product.term.application.port.in.query.dto.TermInfo;
import lombok.Builder;

@Builder
public record TermResponse(
    Long id,
    String link,
    boolean isMandatory
) {
    public static TermResponse from(TermInfo termInfo) {
        return TermResponse.builder()
            .id(termInfo.id())
            .link(termInfo.link())
            .isMandatory(termInfo.isMandatory())
            .build();
    }
}
