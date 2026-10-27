package com.umc.product.project.application.port.out.dto;

import com.umc.product.common.domain.enums.ChallengerPart;

public record ProjectStatisticsMatchedMemberRow(
    Long projectId,
    Long memberId,
    ChallengerPart part,
    Long matchingRoundId
) {
}
