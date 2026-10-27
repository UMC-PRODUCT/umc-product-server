package com.umc.product.project.adapter.in.web.dto.response.statistics;

import com.umc.product.project.application.port.in.query.dto.statistics.ProjectMatchingRoundStatisticsInfo;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "통계에 사용하는 매칭 차수 정보")
public record ProjectMatchingRoundStatisticsResponse(
    @Schema(description = "매칭 차수 ID")
    Long matchingRoundId,
    @Schema(description = "매칭 유형")
    MatchingType type,
    @Schema(description = "매칭 차수")
    MatchingPhase phase
) {
    public static ProjectMatchingRoundStatisticsResponse from(ProjectMatchingRoundStatisticsInfo info) {
        return new ProjectMatchingRoundStatisticsResponse(
            info.matchingRoundId(),
            info.type(),
            info.phase()
        );
    }
}
