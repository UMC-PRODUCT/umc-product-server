package com.umc.product.project.application.port.out.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.ProjectMemberStatus;

public record ProjectStatisticsMemberRow(
    Long projectId,
    Long projectMemberId,
    Long memberId,
    ChallengerPart part,
    ProjectMemberStatus status
) {
}
