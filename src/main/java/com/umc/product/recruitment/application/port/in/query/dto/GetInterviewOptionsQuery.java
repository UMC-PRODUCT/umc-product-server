package com.umc.product.recruitment.application.port.in.query.dto;

public record GetInterviewOptionsQuery(
        Long recruitmentId,
        Long memberId
) {
}
