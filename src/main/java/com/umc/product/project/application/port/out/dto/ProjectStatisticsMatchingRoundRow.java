package com.umc.product.project.application.port.out.dto;

import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;

public record ProjectStatisticsMatchingRoundRow(
    Long matchingRoundId,
    MatchingType matchingRoundType,
    MatchingPhase matchingRoundPhase
) {
}
