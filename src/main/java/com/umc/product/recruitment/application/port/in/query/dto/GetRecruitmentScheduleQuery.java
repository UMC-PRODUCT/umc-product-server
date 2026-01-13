package com.umc.product.recruitment.application.port.in.query.dto;

public record GetRecruitmentScheduleQuery(
        Long recruitmentId,
        Long requesterMemberId
) {
}
