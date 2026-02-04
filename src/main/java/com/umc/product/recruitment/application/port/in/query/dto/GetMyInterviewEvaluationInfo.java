package com.umc.product.recruitment.application.port.in.query.dto;

import java.time.Instant;

public record GetMyInterviewEvaluationInfo(
        MyInterviewEvaluationInfo myEvaluation // 없으면 null
) {
    public record MyInterviewEvaluationInfo(
            Long evaluationId,
            Integer score,
            String comments,
            Instant submittedAt
    ) {
    }
}