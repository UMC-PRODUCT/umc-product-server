package com.umc.product.project.application.port.in.query.dto.statistics;

/**
 * 매칭 차수별 지원 완료 인원 수와 지원 가능 인원 수.
 */
public record RoundApplicationStatisticsInfo(
    ProjectMatchingRoundStatisticsInfo matchingRound,
    long appliedMemberCount,
    long availableMemberCount
) {
}
