package com.umc.product.project.application.port.in.query.dto.statistics;

import java.util.List;

/** PM챌린저 지원통계 집계 결과 (단일 프로젝트 scope). */
public record MyApplicationStatisticsInfo(
    List<RoundStat> roundStats,
    List<SchoolStat> schoolStats
) {
}
