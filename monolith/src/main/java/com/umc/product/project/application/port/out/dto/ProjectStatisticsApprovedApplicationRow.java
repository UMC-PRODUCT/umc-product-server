package com.umc.product.project.application.port.out.dto;

import java.time.Instant;

import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;

public record ProjectStatisticsApprovedApplicationRow(
    Long projectId,
    Long applicantMemberId,
    Long applicationId,
    Long matchingRoundId,
    MatchingType matchingRoundType,
    MatchingPhase matchingRoundPhase,
    Instant matchingRoundStartsAt
) {
}
