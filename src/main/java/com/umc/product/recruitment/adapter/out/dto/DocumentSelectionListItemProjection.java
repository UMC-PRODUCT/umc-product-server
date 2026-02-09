package com.umc.product.recruitment.adapter.out.dto;

import com.umc.product.recruitment.domain.enums.ApplicationStatus;

public record DocumentSelectionListItemProjection(
    Long applicationId,
    String nickname,
    String name,
    ApplicationStatus status
) {
}
