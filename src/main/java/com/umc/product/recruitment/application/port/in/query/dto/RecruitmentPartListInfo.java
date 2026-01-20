package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.domain.enums.ApplicationStatus;
import com.umc.product.recruitment.domain.enums.RecruitmentPartStatus;
import java.time.Instant;
import java.util.List;

public record RecruitmentPartListInfo(
        Long recruitmentId,
        String title,
        DatePeriod recruitmentPeriod,
        DatePeriod activityPeriod,
        String description,
        List<RecruitmentPartSummary> parts,
        MyApplicationInfo myApplication // null: 지원 이력 없음
) {

    public record DatePeriod(
            Instant startsAt,
            Instant endsAt
    ) {
    }

    public record RecruitmentPartSummary(
            Long recruitmentPartId,
            ChallengerPart part,
            RecruitmentPartStatus status
    ) {
    }

    public record MyApplicationInfo(
            Long applicationId,
            Long formResponseId,
            ApplicationStatus status
    ) {
    }

}
