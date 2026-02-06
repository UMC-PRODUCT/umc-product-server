package com.umc.product.recruitment.adapter.out.dto;

public record EvaluationListItemProjection(
        Long evaluationId,
        Long evaluatorMemberId,
        String evaluatorName,
        String evaluatorNickname,
        Integer score,
        String comments
) {
}
