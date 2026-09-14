package com.umc.product.project.application.port.in.query.dto.statistics;

import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;

/**
 * 지원서가 연결된 매칭 차수 정보.
 */
public record ProjectMatchingRoundStatisticsInfo(
    Long matchingRoundId,
    MatchingType type,
    MatchingPhase phase
) {
}
