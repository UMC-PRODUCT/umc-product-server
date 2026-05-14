package com.umc.product.project.application.port.in.query.dto.statistics;

/** 차수별 지원/매칭 집계. quota는 해당 차수 전체 프로젝트 TO 합산. */
public record RoundStat(
    Long roundId,
    long count,
    long quota
) {
}
