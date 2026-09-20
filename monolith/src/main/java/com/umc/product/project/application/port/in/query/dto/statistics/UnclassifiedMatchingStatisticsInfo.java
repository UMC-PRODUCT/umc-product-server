package com.umc.product.project.application.port.in.query.dto.statistics;

import java.util.List;

/**
 * 합격 지원서가 없어 매칭 차수에 귀속할 수 없는 ProjectMember 기준 매칭 통계.
 */
public record UnclassifiedMatchingStatisticsInfo(
    long matchedMemberCount,
    List<ProjectMatchingCountInfo> projects
) {
}
