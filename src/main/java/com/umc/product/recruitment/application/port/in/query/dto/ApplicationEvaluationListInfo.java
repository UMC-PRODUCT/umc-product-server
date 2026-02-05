package com.umc.product.recruitment.application.port.in.query.dto;

import java.math.BigDecimal;
import java.util.List;

public record ApplicationEvaluationListInfo(
        Long recruitmentId,
        Long applicationId,
        BigDecimal avgDocScore,
        List<DocEvaluationSummary> docEvaluationSummaries
) {
    public record DocEvaluationSummary(
            Long evaluationId,
            String evaluatorName,
            String evaluatorNickname,
            Integer score,
            String comments
    ) {
    }
}
