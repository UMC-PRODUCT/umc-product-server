package com.umc.product.recruitment.application.port.in.query.dto;

public record GetRecruitmentDraftApplicationFormQuery(
        Long recruitmentId,
        Long requesterMemberId
) {
}
