package com.umc.product.recruitment.application.port.in.query.dto;

public record GetMyDocumentEvaluationQuery(
        Long recruitmentId,
        Long applicationId,
        Long evaluatorMemberId
) {
}
