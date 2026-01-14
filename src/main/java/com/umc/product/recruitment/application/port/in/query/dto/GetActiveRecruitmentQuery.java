package com.umc.product.recruitment.application.port.in.query.dto;

public record GetActiveRecruitmentQuery(
        Long schoolId,
        Long gisuId
) {
}
