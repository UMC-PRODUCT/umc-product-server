package com.umc.product.recruitment.adapter.out.dto;

import com.umc.product.recruitment.domain.enums.EvaluationStatus;
import java.time.Instant;

public record MyDocumentEvaluationProjection(
    Long applicationId,
    Long evaluationId,
    Integer score,
    String comments,
    EvaluationStatus status,
    Instant updatedAt
) {
    public boolean isSubmitted() {
        return status == EvaluationStatus.SUBMITTED;
    }
}
