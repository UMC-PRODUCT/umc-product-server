package com.umc.product.recruitment.application.port.in.command.dto;

import java.time.Instant;

public record CreateDraftFormResponseInfo(
    Long formId,
    Long formResponseId,
    Instant createdAt

) {
    public static CreateDraftFormResponseInfo from(
        Long formId,
        Long formResponseId,
        Instant createdAt
    ) {
        return new CreateDraftFormResponseInfo(
            formId,
            formResponseId,
            createdAt
        );
    }
}
