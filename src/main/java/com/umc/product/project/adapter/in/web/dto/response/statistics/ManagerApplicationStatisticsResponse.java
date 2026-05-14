package com.umc.product.project.adapter.in.web.dto.response.statistics;

import java.util.List;

/** PROJECT-STAT-001 운영진 지원통계 응답. */
public record ManagerApplicationStatisticsResponse(
    List<RoundStat> roundStats,
    List<SchoolStat> schoolStats,
    List<ProjectRoundStat> projectRoundStats
) {
}
