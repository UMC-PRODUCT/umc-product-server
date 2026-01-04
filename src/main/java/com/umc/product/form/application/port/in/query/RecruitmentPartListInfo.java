package com.umc.product.form.application.port.in.query;

import com.umc.product.form.domain.FormResponseStatus;
import com.umc.product.form.domain.RecruitmentPartStatus;
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
            //ChallengerPart part,
            RecruitmentPartStatus status
    ) {
    }

    public record MyApplicationInfo(
            FormResponseStatus status
    ) {
    }

}
