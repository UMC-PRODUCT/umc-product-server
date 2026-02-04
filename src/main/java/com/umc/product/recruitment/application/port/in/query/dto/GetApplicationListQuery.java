package com.umc.product.recruitment.application.port.in.query.dto;

public record GetApplicationListQuery(
        Long recruitmentId,
        String part,
        String keyword,
        int page,
        int size,
        Long requesterMemberId
) {
}
