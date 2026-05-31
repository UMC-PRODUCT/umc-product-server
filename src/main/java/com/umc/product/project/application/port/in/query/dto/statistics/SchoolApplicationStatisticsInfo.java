package com.umc.product.project.application.port.in.query.dto.statistics;

/**
 * 학교별 지원자 수.
 */
public record SchoolApplicationStatisticsInfo(
    Long schoolId,
    long applicantCount
) {
}
