package com.umc.product.organization.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;

public record PartInfo(
        ChallengerPart part,
        int studyGroupCount,
        int memberCount
) {
}
