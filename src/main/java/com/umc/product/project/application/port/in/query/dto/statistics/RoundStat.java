package com.umc.product.project.application.port.in.query.dto.statistics;

import io.swagger.v3.oas.annotations.media.Schema;

/** 차수별 지원/매칭 집계. quota는 해당 차수 전체 프로젝트 TO 합산. */
@Schema(description = "차수별 지원/매칭 집계")
public record RoundStat(
    @Schema(description = "매칭 차수 ID") Long roundId,
    @Schema(description = "해당 차수 지원자(또는 매칭 멤버) 수") long count,
    @Schema(description = "전체 챌린저 수(분모). ADMIN·PLAN 파트 제외한 gisuId 기준 전체 인원.") long quota
) {
}
