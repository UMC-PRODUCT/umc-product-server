package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.domain.ApplicationStatus;

public record GetApplicationListQuery(
        Long recruitmentId,
        Long requesterMemberId,
        //ChallengerPart partFilter,      // nullable
        ApplicationStatus statusFilter, // nullable
        String keyword
) {
}
