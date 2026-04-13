package com.umc.product.curriculum.adapter.in.web.v2.dto.request;

import com.umc.product.common.domain.enums.ChallengerPart;

public record CreateCurriculumRequest(
    Long gisuId,
    ChallengerPart part,
    String title
) {
}
