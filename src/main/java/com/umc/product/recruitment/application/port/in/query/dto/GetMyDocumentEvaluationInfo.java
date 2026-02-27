package com.umc.product.recruitment.application.port.in.query.dto;

import java.time.Instant;

public record GetMyDocumentEvaluationInfo(
    MyDocumentEvaluationInfo myEvaluation // 없으면 null
) {
    public record MyDocumentEvaluationInfo(
        Long applicationId,
        Long evaluationId,
        Integer score,
        String comments,
        boolean submitted,
        Instant savedAt
    ) {
    }
}
