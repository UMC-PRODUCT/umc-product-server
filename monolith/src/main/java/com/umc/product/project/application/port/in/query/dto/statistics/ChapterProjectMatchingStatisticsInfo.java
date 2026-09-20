package com.umc.product.project.application.port.in.query.dto.statistics;

import java.util.List;

/**
 * 로그인 사용자에게 공개하는 지부 단위 ProjectMember 기준 매칭 요약 통계.
 */
public record ChapterProjectMatchingStatisticsInfo(
    Long chapterId,
    List<RoundMatchingStatisticsInfo> roundMatchingStatistics,
    List<SchoolMatchingStatisticsInfo> schoolMatchingStatistics,
    UnclassifiedMatchingStatisticsInfo unclassifiedMatchingStatistics
) {
}
