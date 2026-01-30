package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.domain.enums.RecruitmentPhase;
import com.umc.product.recruitment.domain.enums.RecruitmentStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record RecruitmentListInfo(
        List<RecruitmentSummary> recruitments
) {
    public record RecruitmentSummary(
            Long recruitmentId,
            String recruitmentName,
            LocalDate startDate,
            LocalDate endDate,
            int applicantCount,
            RecruitmentStatus status,
            RecruitmentPhase phase,
            boolean editable,
            Instant updatedAt
    ) {
    }
}
