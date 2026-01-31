package com.umc.product.recruitment.application.port.in.query.dto;

public record GetPublishedRecruitmentDetailQuery(
        Long memberId,
        Long recruitmentId
) {
}
