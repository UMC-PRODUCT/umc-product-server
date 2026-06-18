package com.umc.product.project.application.port.in.query.dto.statistics;

import java.util.List;

/**
 * 매칭 차수별 지원자 학교 인원 수.
 */
public record RoundSchoolApplicationStatisticsInfo(
    ProjectMatchingRoundStatisticsInfo matchingRound,
    List<SchoolApplicationStatisticsInfo> schools
) {
}
