package com.umc.product.recruitment.application.port.in.query;

public record GetRecruitmentPartListQuery(
        Long recruitmentId,
        Long userId
) {
}
