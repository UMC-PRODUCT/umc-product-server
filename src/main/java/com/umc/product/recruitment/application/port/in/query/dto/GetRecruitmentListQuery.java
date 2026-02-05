package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.application.port.in.query.RecruitmentListStatus;

public record GetRecruitmentListQuery(
    Long requesterMemberId,
    RecruitmentListStatus status
) {
}
