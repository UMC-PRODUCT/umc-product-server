package com.umc.product.recruitment.application.port.in.query.dto;

public record GetActiveRecruitmentQuery(
        Long requesterMemberId,
        Long schoolId,   // nullable
        Long gisuId      // nullable
) {
}
