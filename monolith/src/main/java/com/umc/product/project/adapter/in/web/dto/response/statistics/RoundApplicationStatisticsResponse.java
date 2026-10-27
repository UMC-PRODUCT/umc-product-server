package com.umc.product.project.adapter.in.web.dto.response.statistics;

import com.umc.product.project.application.port.in.query.dto.statistics.RoundApplicationStatisticsInfo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "차수별 지원 현황")
public record RoundApplicationStatisticsResponse(
    @Schema(description = "매칭 차수")
    ProjectMatchingRoundStatisticsResponse matchingRound,
    @Schema(description = "지원 인원 수")
    long appliedMemberCount,
    @Schema(description = "지원 가능 인원 수. 이전 차수까지 매칭된 인원을 뺀 값입니다.")
    long availableMemberCount
) {
    public static RoundApplicationStatisticsResponse from(RoundApplicationStatisticsInfo info) {
        return new RoundApplicationStatisticsResponse(
            ProjectMatchingRoundStatisticsResponse.from(info.matchingRound()),
            info.appliedMemberCount(),
            info.availableMemberCount()
        );
    }
}
