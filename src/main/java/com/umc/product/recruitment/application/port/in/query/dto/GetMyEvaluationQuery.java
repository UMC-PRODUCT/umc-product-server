package com.umc.product.recruitment.application.port.in.query.dto;

public record GetMyEvaluationQuery(
        Long recruitmentId,
        Long applicationId,
        Long evaluatorMemberId
) {
}
