package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.application.port.in.PartOption;

public record GetInterviewSchedulingAssignmentsQuery(
    Long recruitmentId,
    Long slotId,
    PartOption part,
    Long requesterId
) {
}
