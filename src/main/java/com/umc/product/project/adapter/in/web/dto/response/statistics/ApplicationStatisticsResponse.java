package com.umc.product.project.adapter.in.web.dto.response.statistics;

import com.umc.product.project.application.port.in.query.dto.statistics.ApplicationStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectRoundStat;
import com.umc.product.project.application.port.in.query.dto.statistics.RoundStat;
import com.umc.product.project.application.port.in.query.dto.statistics.SchoolStat;
import java.util.List;

/** PROJECT-STAT-001/002 지원통계 응답. SchoolStat.total은 PM챌린저 뷰에서 null. */
public record ApplicationStatisticsResponse(
    List<RoundStat> roundStats,
    List<SchoolStat> schoolStats,
    List<ProjectRoundStat> projectRoundStats
) {
    public static ApplicationStatisticsResponse from(ApplicationStatisticsInfo info) {
        return new ApplicationStatisticsResponse(
            info.roundStats(),
            info.schoolStats(),
            info.projectRoundStats()
        );
    }
}
