package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.application.port.in.PartOption;

public record GetInterviewSchedulingApplicantsQuery(
    Long recruitmentId,
    Long slotId,
    PartOption part,
    String keyword,
    Long requesterId
) {
}
