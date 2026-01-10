package com.umc.product.survey.application.port.in.query;

import com.umc.product.survey.domain.FormResponseStatus;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record DraftFormResponseInfo(
        Long formResponseId,
        Long recruitmentId,
        Long formId,
        FormResponseStatus status, // 항상 DRAFT
        Instant lastSavedAt,
        List<AnswerInfo> answers
) {

    public record AnswerInfo(
            Long questionId,
            Map<String, Object> value
    ) {
    }

}
