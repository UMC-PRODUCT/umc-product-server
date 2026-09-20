package com.umc.product.project.application.port.in.query.dto.statistics;

import java.util.List;

/**
 * 프로젝트별 매칭 차수 인원 수.
 */
public record ProjectRoundMemberStatisticsInfo(
    Long projectId,
    List<ProjectRoundMemberCountInfo> matchingRounds
) {
}
