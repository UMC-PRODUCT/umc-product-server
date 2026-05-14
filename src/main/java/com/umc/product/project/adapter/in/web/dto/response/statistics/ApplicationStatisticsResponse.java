package com.umc.product.project.adapter.in.web.dto.response.statistics;

import com.umc.product.project.application.port.in.query.dto.statistics.ApplicationStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectRoundStat;
import com.umc.product.project.application.port.in.query.dto.statistics.RoundStat;
import com.umc.product.project.application.port.in.query.dto.statistics.SchoolStat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** PROJECT-STAT-001/002 지원통계 응답. SchoolStat.total은 PM챌린저 뷰에서 null. */
@Schema(description = "지원통계 응답. 호출자 역할에 따라 집계 범위가 달라집니다. (운영진: 지부 전체 / PM챌린저: 본인 프로젝트)")
public record ApplicationStatisticsResponse(
    @Schema(description = "차수별 지원 집계. 운영진/PM챌린저 공통 제공.") List<RoundStat> roundStats,
    @Schema(description = "학교 × 차수별 지원 집계. schoolStat.total은 운영진만 제공(PM챌린저는 null).") List<SchoolStat> schoolStats,
    @Schema(description = "프로젝트 × 차수별 지원 집계.") List<ProjectRoundStat> projectRoundStats
) {
    public static ApplicationStatisticsResponse from(ApplicationStatisticsInfo info) {
        return new ApplicationStatisticsResponse(
            info.roundStats(),
            info.schoolStats(),
            info.projectRoundStats()
        );
    }
}
