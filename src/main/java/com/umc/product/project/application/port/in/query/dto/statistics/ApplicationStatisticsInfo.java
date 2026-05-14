package com.umc.product.project.application.port.in.query.dto.statistics;

import java.util.List;

/** 운영진 지원통계 집계 결과. */
public record ApplicationStatisticsInfo(
    List<RoundStat> roundStats,
    List<SchoolStat> schoolStats,
    List<ProjectRoundStat> projectRoundStats
) {
}
