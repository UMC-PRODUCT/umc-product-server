package com.umc.product.project.application.port.in.query.dto.statistics;

/** 학교 × 차수별 지원/매칭 집계. total은 해당 학교 총원(분모). PM챌린저 뷰에서는 null. */
public record SchoolStat(
    Long schoolId,
    Long roundId,
    long count,
    Long total
) {
}
