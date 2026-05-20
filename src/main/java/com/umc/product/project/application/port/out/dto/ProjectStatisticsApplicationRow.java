package com.umc.product.project.application.port.out.dto;

import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;

public record ProjectStatisticsApplicationRow(
    Long projectId,
    Long applicantMemberId,
    Long applicationId,
    ProjectApplicationStatus status,
    Long matchingRoundId,
    MatchingType matchingRoundType,
    MatchingPhase matchingRoundPhase
) {
}
