package com.umc.product.recruitment.adapter.in.web.dto.response;

import java.time.Instant;

public record RecruitmentDraftFormResponseResponse(
    Long recruitmentId,
    Long formId,
    Long formResponseId,
    Instant createdAt
) {
    public static RecruitmentDraftFormResponseResponse from(
        Long recruitmentId,
        Long formId,
        Long formResponseId,
        Instant createdAt
    ) {
        return new RecruitmentDraftFormResponseResponse(
            recruitmentId,
            formId,
            formResponseId,
            createdAt
        );
    }
}
