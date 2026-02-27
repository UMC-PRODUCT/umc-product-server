package com.umc.product.recruitment.application.port.in.query.dto;

public record GetRecruitmentPartListQuery(
    Long recruitmentId,
    Long memberId
) {
}
