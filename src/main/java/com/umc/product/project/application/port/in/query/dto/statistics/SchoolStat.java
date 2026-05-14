package com.umc.product.project.application.port.in.query.dto.statistics;

/** 학교 × 차수별 지원/매칭 집계. total은 차수 무관 해당 학교 총원(분모). */
public record SchoolStat(
    Long schoolId,
    Long roundId,
    long count,
    long total
) {
}
