package com.umc.product.recruitment.adapter.out.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.domain.enums.ApplicationStatus;

public record FinalSelectionListItemProjection(
    Long applicationId,
    String nickname,
    String name,
    ApplicationStatus status,
    ChallengerPart selectedPart
) {
}
