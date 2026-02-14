package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.domain.enums.PartKey;
import java.time.Instant;
import java.util.List;

public record DocumentEvaluationRecruitmentListInfo(
    List<DocumentEvaluationRecruitmentInfo> recruitments
) {
    public record DocumentEvaluationRecruitmentInfo(
        Long recruitmentId,
        Long rootRecruitmentId,
        String title,
        Instant docReviewStartAt,
        Instant docReviewEndAt,
        Long totalApplicantCount,
        List<PartKey> openParts
    ) {
    }
}
