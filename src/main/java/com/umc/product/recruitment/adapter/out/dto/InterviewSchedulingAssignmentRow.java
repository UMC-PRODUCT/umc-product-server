package com.umc.product.recruitment.adapter.out.dto;

import com.umc.product.common.domain.enums.ChallengerPart;

public record InterviewSchedulingAssignmentRow(
    Long assignmentId,
    Long applicationId,
    String nickname,
    String name,
    ChallengerPart firstPart,
    ChallengerPart secondPart
) {
}
