package com.umc.product.project.application.port.in.query.dto.statistics;

/**
 * 프로젝트 내 특정 매칭 차수의 지원 완료 인원 수와 매칭 완료 인원 수.
 */
public record ProjectRoundMemberCountInfo(
    ProjectMatchingRoundStatisticsInfo matchingRound,
    long appliedMemberCount,
    long matchedMemberCount
) {
}
