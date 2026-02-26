package com.umc.product.recruitment.application.port.in.query.dto;

public record GetApplicationDetailQuery(
    Long recruitmentId,
    Long applicationId,
    Long requesterMemberId
) {
}
