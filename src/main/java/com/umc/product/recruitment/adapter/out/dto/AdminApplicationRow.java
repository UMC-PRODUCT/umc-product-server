package com.umc.product.recruitment.adapter.out.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.domain.enums.ApplicationStatus;

public record AdminApplicationRow(
    Long applicationId,
    Long applicantMemberId,
    String nickname,
    String name,
    String email,
    Long applicantSchoolId,
    String applicantSchoolName,
    ApplicationStatus applicationStatus,
    ChallengerPart selectedPart // FINAL_ACCEPTED일 때만 (nullable)
) {
}
