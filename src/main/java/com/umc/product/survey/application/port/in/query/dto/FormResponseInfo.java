package com.umc.product.survey.application.port.in.query.dto;

import com.umc.product.survey.domain.enums.FormResponseStatus;
import java.time.Instant;
import java.util.List;

public record FormResponseInfo(
        Long formResponseId,
        Long formId,
        FormResponseStatus status, // DRAFT or SUBMITTED
        Instant lastSavedAt,
        Instant submittedAt,
        List<AnswerInfo> answers
) {
}
