package com.umc.product.recruitment.application.port.in.query.dto;

public record GetMyInterviewEvaluationQuery(
        Long recruitmentId,
        Long assignmentId,
        Long memberId
) {
}
