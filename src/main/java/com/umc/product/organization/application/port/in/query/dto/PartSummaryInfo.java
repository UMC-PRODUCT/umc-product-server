package com.umc.product.organization.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;

public record PartSummaryInfo(
        Long schoolId,
        String schoolName,
        List<PartInfo> parts
) {
    public record PartInfo(
            ChallengerPart part,
            int studyGroupCount,
            int memberCount
    ) {
    }
}
