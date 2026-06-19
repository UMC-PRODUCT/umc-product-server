package com.umc.product.project.application.port.in.query.dto.statistics;

import java.util.List;

/**
 * 매칭 차수별 ProjectMember 기준 매칭 완료 인원 수와 지원 가능 인원 수.
 */
public record RoundMatchingStatisticsInfo(
    ProjectMatchingRoundStatisticsInfo matchingRound,
    long matchedMemberCount,
    long availableMemberCount,
    List<ProjectMatchingCountInfo> projects
) {
}
