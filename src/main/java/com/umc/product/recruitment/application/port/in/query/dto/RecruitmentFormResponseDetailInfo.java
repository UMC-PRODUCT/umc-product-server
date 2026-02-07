package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.survey.application.port.in.query.dto.AnswerInfo;
import com.umc.product.survey.domain.enums.FormResponseStatus;
import java.time.Instant;
import java.util.List;

public record RecruitmentFormResponseDetailInfo(
    Long formId,
    Long formResponseId,
    FormResponseStatus status,   // DRAFT / SUBMITTED
    Instant lastSavedAt,
    Instant submittedAt,
    List<AnswerInfo> answers
) {
}
