package com.umc.product.project.adapter.in.web.dto.response.statistics;

import com.umc.product.project.application.port.in.query.dto.statistics.ProjectRoundStat;
import com.umc.product.project.application.port.in.query.dto.statistics.RoundStat;
import com.umc.product.project.application.port.in.query.dto.statistics.SchoolStat;
import java.util.List;

/** PROJECT-STAT-003 운영진 매칭통계 응답. 집계 대상은 ACTIVE ProjectMember(매칭 완료자) 기준. */
public record ManagerMatchingStatisticsResponse(
    List<RoundStat> roundStats,
    List<SchoolStat> schoolStats,
    List<ProjectRoundStat> projectRoundStats
) {
}
