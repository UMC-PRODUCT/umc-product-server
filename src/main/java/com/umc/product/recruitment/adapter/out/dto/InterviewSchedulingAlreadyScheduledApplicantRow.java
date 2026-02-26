package com.umc.product.recruitment.adapter.out.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.time.Instant;

public record InterviewSchedulingAlreadyScheduledApplicantRow(
    Long applicationId,
    Long assignmentId,
    String nickname,
    String name,
    ChallengerPart firstPart,
    ChallengerPart secondPart,
    Instant slotStartsAt,
    Instant slotEndsAt
) {
}
