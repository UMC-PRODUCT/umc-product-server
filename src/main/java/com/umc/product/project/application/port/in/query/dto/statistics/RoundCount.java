package com.umc.product.project.application.port.in.query.dto.statistics;

import io.swagger.v3.oas.annotations.media.Schema;

/** 차수별 지원/매칭 인원 수. SchoolStat·ProjectStat의 내부 차수 집계에 사용. */
@Schema(description = "차수별 지원/매칭 인원 수")
public record RoundCount(
    @Schema(description = "매칭 차수 ID")
    Long roundId,
    @Schema(description = "해당 차수 지원자(또는 매칭 멤버) 수")
    long count
) {
}
