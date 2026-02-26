package com.umc.product.recruitment.application.port.in.query.dto;

public record GetInterviewEvaluationViewQuery(
    Long recruitmentId,
    Long assignmentId,
    Long memberId
) {
}
