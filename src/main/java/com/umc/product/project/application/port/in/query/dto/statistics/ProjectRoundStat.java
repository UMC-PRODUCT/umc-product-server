package com.umc.product.project.application.port.in.query.dto.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** 프로젝트별 차수 집계. 운영진 전용. */
@Schema(description = "프로젝트별 차수 집계")
public record ProjectRoundStat(
    @Schema(description = "프로젝트 ID") Long projectId,
    @Schema(description = "차수별 집계 목록. roundId 오름차순 정렬.") List<RoundCount> rounds
) {
}
