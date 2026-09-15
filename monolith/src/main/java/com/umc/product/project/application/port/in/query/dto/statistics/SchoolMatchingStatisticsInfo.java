package com.umc.product.project.application.port.in.query.dto.statistics;

/**
 * 학교별 매칭 완료 인원 수와 총원.
 */
public record SchoolMatchingStatisticsInfo(
    Long schoolId,
    long matchedMemberCount,
    long totalMemberCount
) {
}
