package com.umc.product.project.application.port.in.query.dto.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** 프로젝트별 차수 집계. 운영진 전용. */
@Schema(description = "프로젝트별 지원 현황 섹션 및 N차 매칭 지원 현황")
public record ProjectApplicantStatistics(
    @Schema(description = "프로젝트 ID")
    Long projectId,
    @Schema(description = "차수별 집계 목록. roundId 오름차순 정렬.")
    List<ProjectApplicantCountPerRound> matchingRoundStatistics
) {
    /** 차수별 지원/매칭 인원 수. SchoolStat·ProjectStat의 내부 차수 집계에 사용. */
    @Schema(description = "차수별 지원/매칭 인원 수")
    public record ProjectApplicantCountPerRound(
        @Schema(description = "매칭 차수 ID")
        Long matchingRoundId,
        @Schema(description = "해당 차수 지원자(또는 매칭 멤버) 수")
        long count
    ) {
    }
}
