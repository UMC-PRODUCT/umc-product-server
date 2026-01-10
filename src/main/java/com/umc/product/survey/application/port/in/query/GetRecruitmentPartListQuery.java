package com.umc.product.survey.application.port.in.query;

public record GetRecruitmentPartListQuery(
        Long recruitmentId,
        Long userId
) {
}
