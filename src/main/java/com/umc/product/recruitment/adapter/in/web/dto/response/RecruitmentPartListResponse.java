package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentPartListInfo;
import com.umc.product.recruitment.domain.ApplicationStatus;
import com.umc.product.recruitment.domain.RecruitmentPartStatus;
import java.time.Instant;
import java.util.List;

public record RecruitmentPartListResponse(
        Long recruitmentId,
        String title,
        DatePeriodResponse recruitmentPeriod,
        DatePeriodResponse activityPeriod,
        String description,
        List<RecruitmentPartSummaryResponse> parts,
        MyApplicationResponse myApplication // null: 지원 이력 없음
) {
    public static RecruitmentPartListResponse from(RecruitmentPartListInfo info) {
        return new RecruitmentPartListResponse(
                info.recruitmentId(),
                info.title(),
                DatePeriodResponse.from(info.recruitmentPeriod()),
                DatePeriodResponse.from(info.activityPeriod()),
                info.description(),
                info.parts().stream().map(RecruitmentPartSummaryResponse::from).toList(),
                info.myApplication() == null ? null : MyApplicationResponse.from(info.myApplication())
        );
    }

    public record DatePeriodResponse(
            Instant startsAt,
            Instant endsAt
    ) {
        public static DatePeriodResponse from(RecruitmentPartListInfo.DatePeriod period) {
            return period == null ? null : new DatePeriodResponse(period.startsAt(), period.endsAt());
        }
    }

    public record RecruitmentPartSummaryResponse(
            Long recruitmentPartId,
            ChallengerPart part,
            RecruitmentPartStatus status
    ) {
        public static RecruitmentPartSummaryResponse from(RecruitmentPartListInfo.RecruitmentPartSummary p) {
            return new RecruitmentPartSummaryResponse(
                    p.recruitmentPartId(),
                    p.part(),
                    p.status()
            );
        }
    }

    public record MyApplicationResponse(
            Long applicationId,
            Long formResponseId,
            ApplicationStatus status
    ) {
        public static MyApplicationResponse from(RecruitmentPartListInfo.MyApplicationInfo a) {
            return new MyApplicationResponse(a.applicationId(), a.formResponseId(), a.status());
        }
    }
}
