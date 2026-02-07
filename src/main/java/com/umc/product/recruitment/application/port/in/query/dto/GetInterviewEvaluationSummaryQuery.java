package com.umc.product.recruitment.application.port.in.query.dto;

public record GetInterviewEvaluationSummaryQuery(
    Long recruitmentId,
    Long assignmentId,
    Long memberId
) {
}
