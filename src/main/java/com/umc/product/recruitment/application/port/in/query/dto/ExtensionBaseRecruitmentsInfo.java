package com.umc.product.recruitment.application.port.in.query.dto;

import java.time.Instant;
import java.util.List;

public record ExtensionBaseRecruitmentsInfo(
    List<ExtensionBaseRecruitmentInfo> recruitments
) {
    public record ExtensionBaseRecruitmentInfo(
        Long recruitmentId,
        String title,
        boolean isRoot,
        Instant applyStartAt,
        Instant finalResultAt
    ) {
    }
}
