package com.umc.product.project.application.port.in.query.dto.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** 학교별 지원/매칭 집계. total은 해당 학교 총원(분모). PM챌린저 뷰에서는 null. */
@Schema(description = "학교별 지원/매칭 집계")
public record SchoolStat(
    @Schema(description = "학교 ID")
    Long schoolId,
    @Schema(description = "해당 학교 전체 챌린저 수(분모). ADMIN·PLAN 파트 제외. 운영진만 제공되며 PM챌린저 호출 시 null.", nullable = true)
    Long total,
    @Schema(description = "차수별 집계 목록. roundId 오름차순 정렬.")
    List<RoundCount> rounds
) {
}
