package com.umc.product.project.application.port.in.query.dto.statistics;

/** 프로젝트 × 차수별 지원/매칭 집계. */
public record ProjectRoundStat(
    Long projectId,
    Long roundId,
    long count
) {
}
