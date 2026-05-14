package com.umc.product.project.application.port.in.query.dto.statistics;

import java.util.List;

/** 운영진 매칭통계 집계 결과. */
public record MatchingStatisticsInfo(
    List<RoundStat> roundStats,
    List<SchoolStat> schoolStats,
    List<ProjectRoundStat> projectRoundStats
) {
}
