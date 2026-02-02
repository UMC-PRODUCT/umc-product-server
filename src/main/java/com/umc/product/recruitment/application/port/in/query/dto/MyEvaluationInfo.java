package com.umc.product.recruitment.application.port.in.query.dto;

import java.time.Instant;

public record MyEvaluationInfo(
        Long applicationId,
        Long evaluationId,
        Integer score,
        String comment,
        Instant updatedAt
) {
}