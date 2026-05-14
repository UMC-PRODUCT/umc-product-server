package com.umc.product.project.application.port.in.query.dto.statistics;

import io.swagger.v3.oas.annotations.media.Schema;

/** 학교 × 차수별 지원/매칭 집계. total은 해당 학교 총원(분모). PM챌린저 뷰에서는 null. */
@Schema(description = "학교 × 차수별 지원/매칭 집계")
public record SchoolStat(
    @Schema(description = "학교 ID") Long schoolId,
    @Schema(description = "매칭 차수 ID") Long roundId,
    @Schema(description = "해당 학교 × 차수 지원자(또는 매칭 멤버) 수") long count,
    @Schema(description = "해당 학교 전체 챌린저 수(분모). ADMIN·PLAN 파트 제외. 운영진만 제공되며 PM챌린저 호출 시 null.", nullable = true) Long total
) {
}
