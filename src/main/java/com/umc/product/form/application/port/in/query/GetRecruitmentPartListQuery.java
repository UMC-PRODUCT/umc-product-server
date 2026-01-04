package com.umc.product.form.application.port.in.query;

public record GetRecruitmentPartListQuery(
        Long recruitmentId,
        Long userId
) {
}
