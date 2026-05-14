package com.umc.product.project.application.port.in.query.dto.statistics;

import io.swagger.v3.oas.annotations.media.Schema;

/** 프로젝트 × 차수별 지원/매칭 집계. */
@Schema(description = "프로젝트 × 차수별 지원/매칭 집계")
public record ProjectRoundStat(
    @Schema(description = "프로젝트 ID") Long projectId,
    @Schema(description = "매칭 차수 ID") Long roundId,
    @Schema(description = "해당 프로젝트 × 차수 지원자(또는 매칭 멤버) 수") long count
) {
}
