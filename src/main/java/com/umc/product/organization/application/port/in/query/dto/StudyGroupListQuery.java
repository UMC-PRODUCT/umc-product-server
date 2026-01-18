package com.umc.product.organization.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;

public record StudyGroupListQuery(
        Long schoolId,
        ChallengerPart part,
        Long cursor,
        int size
) {
    public StudyGroupListQuery {
        if (size <= 0) {
            size = 20;
        }
        if (size > 100) {
            size = 100;
        }
    }
}
