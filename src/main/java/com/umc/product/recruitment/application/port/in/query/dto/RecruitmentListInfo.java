package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.domain.RecruitmentPhase;
import java.util.List;

public record RecruitmentListInfo(
        List<RecruitmentSummary> recruitments
) {
    public record RecruitmentSummary(
            Long recruitmentId,
            String title,
            RecruitmentPhase phase,
            boolean isActive
    ) {
    }
}
