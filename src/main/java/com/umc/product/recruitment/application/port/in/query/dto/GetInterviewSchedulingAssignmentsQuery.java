package com.umc.product.recruitment.application.port.in.query.dto;

public record GetInterviewSchedulingAssignmentsQuery(
        Long recruitmentId,
        Long slotId,
        String part,
        Long requesterId
) {
}
