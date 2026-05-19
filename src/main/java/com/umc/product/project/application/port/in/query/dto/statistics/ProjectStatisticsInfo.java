package com.umc.product.project.application.port.in.query.dto.statistics;

import java.util.List;

/**
 * 프로젝트별 지원/매칭 현황.
 */
public record ProjectStatisticsInfo(
    Long projectId,
    List<ProjectMemberStatisticsInfo> projectMembers
) {
}
