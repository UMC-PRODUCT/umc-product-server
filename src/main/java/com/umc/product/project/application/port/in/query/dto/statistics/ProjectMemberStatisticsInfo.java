package com.umc.product.project.application.port.in.query.dto.statistics;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.ProjectMemberStatus;
import java.util.List;

/**
 * 프로젝트에 최종 합류한 멤버와 해당 프로젝트에 작성한 지원 이력.
 */
public record ProjectMemberStatisticsInfo(
    Long projectMemberId,
    Long memberId,
    ChallengerPart part,
    ProjectMemberStatus status,
    List<ProjectMemberApplicationStatisticsInfo> applications
) {
}
