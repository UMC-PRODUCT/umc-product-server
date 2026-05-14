package com.umc.product.project.adapter.in.web.dto.response.statistics;

import com.umc.product.project.application.port.in.query.dto.statistics.RoundStat;
import com.umc.product.project.application.port.in.query.dto.statistics.SchoolStat;
import java.util.List;

/** PROJECT-STAT-002 PM챌린저 지원통계 응답. */
public record MyApplicationStatisticsResponse(
    List<RoundStat> roundStats,
    List<SchoolStat> schoolStats
) {
}
