package com.umc.product.recruitment.application.port.in.query;

public record GetActiveRecruitmentQuery(
        Long schoolId,
        Long gisuId
) {
}
