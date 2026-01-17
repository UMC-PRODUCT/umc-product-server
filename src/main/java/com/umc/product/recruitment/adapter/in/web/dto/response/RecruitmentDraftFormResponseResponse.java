package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.survey.application.port.in.query.dto.DraftFormResponseInfo;
import com.umc.product.survey.domain.enums.FormResponseStatus;
import java.time.Instant;

public record RecruitmentDraftFormResponseResponse(
        Long recruitmentId,
        Long formId,
        Long formResponseId,
        FormResponseStatus status, //DRAFT
        boolean created, // true면 이번 호출로 새로 생성됨
        Instant createdAt
) {
    public static RecruitmentDraftFormResponseResponse from(
            Long recruitmentId,
            DraftFormResponseInfo info,
            boolean created
    ) {
        return new RecruitmentDraftFormResponseResponse(
                recruitmentId,
                info.formId(),
                info.formResponseId(),
                info.status(),
                created,
                info.createdAt()
        );
    }
}
