package com.umc.product.project.application.port.out.dto;

import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;

public record ProjectMemberMatchedRoundInfo(
    Long projectId,
    Long memberId,
    Long matchingRoundId,
    MatchingType matchingRoundType,
    MatchingPhase matchingRoundPhase
) {
}
