package com.umc.product.project.application.port.in.query.dto.statistics;

import com.umc.product.project.domain.enums.ProjectApplicationStatus;

/**
 * 프로젝트 멤버가 해당 프로젝트에 작성한 차수별 지원 이력.
 */
public record ProjectMemberApplicationStatisticsInfo(
    Long applicationId,
    ProjectApplicationStatus status,
    ProjectMatchingRoundStatisticsInfo matchingRound
) {
}
