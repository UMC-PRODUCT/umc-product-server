package com.umc.product.recruitment.application.port.in.query;

public record GetApplicationDetailQuery(
        Long applicationId,
        Long requesterMemberId
) {
}
