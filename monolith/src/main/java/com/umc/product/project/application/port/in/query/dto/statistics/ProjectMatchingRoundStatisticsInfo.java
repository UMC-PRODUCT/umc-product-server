package com.umc.product.project.application.port.in.query.dto.statistics;

import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;

/**
 * 통계에 사용하는 매칭 차수 정보.
 */
public record ProjectMatchingRoundStatisticsInfo(
    Long matchingRoundId,
    MatchingType type,
    MatchingPhase phase
) {
}
