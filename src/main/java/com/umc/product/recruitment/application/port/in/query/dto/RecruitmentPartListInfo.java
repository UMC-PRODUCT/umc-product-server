package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
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
        MyApplicationInfo myApplication
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
            MyApplicationStatus status,
            Long draftFormResponseId, // status=DRAFT 일 때만 세팅
            Long applicationId        // status=SUBMITTED 일 때만 세팅
    ) {
        public static MyApplicationInfo none() {
            return new MyApplicationInfo(MyApplicationStatus.NONE, null, null);
        }

        public static MyApplicationInfo draft(Long draftFormResponseId) {
            return new MyApplicationInfo(MyApplicationStatus.DRAFT, draftFormResponseId, null);
        }

        public static MyApplicationInfo submitted(Long applicationId) {
            return new MyApplicationInfo(MyApplicationStatus.SUBMITTED, null, applicationId);
        }
    }

    public enum MyApplicationStatus {NONE, DRAFT, SUBMITTED}

}
