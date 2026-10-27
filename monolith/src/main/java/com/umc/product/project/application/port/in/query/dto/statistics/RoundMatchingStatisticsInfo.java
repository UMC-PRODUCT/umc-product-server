package com.umc.product.project.application.port.in.query.dto.statistics;

import com.umc.product.project.domain.enums.MatchingPhase;

/**
 * 차수별 매칭 완료 인원 수와 매칭 대기 인원 수.
 */
public record RoundMatchingStatisticsInfo(
    MatchingPhase phase,
    long matchedMemberCount,
    long availableMemberCount
) {
}
