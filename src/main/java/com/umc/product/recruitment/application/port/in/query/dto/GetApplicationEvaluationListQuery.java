package com.umc.product.recruitment.application.port.in.query.dto;

public record GetApplicationEvaluationListQuery(
        Long recruitmentId,
        Long applicationId,
        Long requesterMemberId
) {
}
