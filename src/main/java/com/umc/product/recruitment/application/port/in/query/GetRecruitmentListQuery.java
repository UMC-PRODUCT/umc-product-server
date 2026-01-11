package com.umc.product.recruitment.application.port.in.query;

public record GetRecruitmentListQuery(
        Long requesterMemberId,
        RecruitmentListStatus status
) {
}
