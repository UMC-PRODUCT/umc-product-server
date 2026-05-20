package com.umc.product.project.application.port.in.query.dto.statistics;

import java.util.List;

/**
 * 지부 전체 프로젝트 지원/매칭 현황 BFF 응답 정보.
 */
public record ChapterProjectStatisticsInfo(
    Long chapterId,
    List<ProjectStatisticsInfo> projects,
    ChapterProjectStatisticsSummaryInfo summary
) {
}
