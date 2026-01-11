package com.umc.product.survey.application.port.in.query;

import com.umc.product.survey.domain.FormResponseStatus;
import java.time.Instant;
import java.util.List;

public record DraftFormResponseInfo(
        Long formResponseId,
        Long formId,
        FormResponseStatus status, // 항상 DRAFT
        Instant lastSavedAt,
        List<AnswerInfo> answers
) {
}
