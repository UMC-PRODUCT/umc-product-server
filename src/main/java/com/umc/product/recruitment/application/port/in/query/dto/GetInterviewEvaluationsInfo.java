package com.umc.product.recruitment.application.port.in.query.dto;

import java.util.List;

public record GetInterviewEvaluationsInfo(
        Double avgScore,
        List<GetInterviewEvaluationInfo> items
) {
    public record GetInterviewEvaluationInfo(
            Evaluator evaluator,
            Integer score,
            String comments
    ) {
    }

    public record Evaluator(Long memberId, String nickname, String name) {
    }
}
