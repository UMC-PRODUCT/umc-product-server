package com.umc.product.term.adapter.in.web.dto.response;

import java.time.Instant;
import java.util.List;

import com.umc.product.term.application.port.in.query.dto.ActiveTermInfo;
import com.umc.product.term.domain.enums.TermType;

public record ActiveTermsResponse(
    List<ActiveTermResponse> terms
) {

    public ActiveTermsResponse {
        terms = List.copyOf(terms);
    }

    public static ActiveTermsResponse from(List<ActiveTermInfo> activeTerms) {
        return new ActiveTermsResponse(
            activeTerms.stream()
                .map(ActiveTermResponse::from)
                .toList()
        );
    }

    public record ActiveTermResponse(
        Long id,
        TermType type,
        String typeDescription,
        String link,
        boolean isMandatory,
        Long version,
        Instant createdAt,
        Instant updatedAt
    ) {

        public static ActiveTermResponse from(ActiveTermInfo activeTermInfo) {
            return new ActiveTermResponse(
                activeTermInfo.id(),
                activeTermInfo.type(),
                activeTermInfo.typeDescription(),
                activeTermInfo.link(),
                activeTermInfo.isMandatory(),
                activeTermInfo.version(),
                activeTermInfo.createdAt(),
                activeTermInfo.updatedAt()
            );
        }
    }
}
