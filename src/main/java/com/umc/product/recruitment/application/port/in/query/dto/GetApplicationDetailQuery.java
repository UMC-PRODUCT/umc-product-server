package com.umc.product.recruitment.application.port.in.query.dto;

public record GetApplicationDetailQuery(
        Long applicationId,
        Long requesterMemberId
) {
}
