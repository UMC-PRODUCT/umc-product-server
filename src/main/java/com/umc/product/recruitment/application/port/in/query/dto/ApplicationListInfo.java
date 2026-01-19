package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.domain.enums.ApplicationStatus;
import java.util.List;

public record ApplicationListInfo(
        Long recruitmentId,
        List<ApplicationSummary> applications
) {
    public record ApplicationSummary(
            Long applicationId,
            Long applicantMemberId,
            String name,
            String nickname,
            ChallengerPart appliedPart,     // 1지망 or 확정파트 등 정책에 맞게
            ApplicationStatus status,
            Integer docTotalScore
    ) {
    }
}
