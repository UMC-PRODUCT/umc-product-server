package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.domain.RecruitmentPhase;
import java.time.LocalDate;
import java.util.List;

public record RecruitmentListInfo(
        List<RecruitmentSummary> recruitments
) {
    public record RecruitmentSummary(
            String schoolName,
            String gisu,
            Long recruitmentId,
            String recruitmentName,
            LocalDate startDate,
            LocalDate endDate,
            int applicantCount,
            RecruitmentPhase phase,
            boolean editable
    ) {
    }
}
