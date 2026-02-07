package com.umc.product.recruitment.application.port.in.query.dto;

public record GetInterviewSheetPartsQuery(
    Long recruitmentId,
    Long requesterMemberId
) {
}
