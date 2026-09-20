package com.umc.product.curriculum.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;

public record CurriculumProjection(
    Long id,
    ChallengerPart part,
    String title
) {
}
