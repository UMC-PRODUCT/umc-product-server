package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentPartListInfo;
import com.umc.product.recruitment.domain.enums.RecruitmentPartStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public record RecruitmentPartListResponse(
    Long recruitmentId,
    String title,
    DatePeriodResponse recruitmentPeriod,
    DatePeriodResponse activityPeriod,
    String description,
    List<RecruitmentPartSummaryResponse> parts,
    MyApplicationResponse myApplication
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
        LocalDate startsAt,
        LocalDate endsAt
    ) {
        public static DatePeriodResponse from(RecruitmentPartListInfo.DatePeriod period) {
            if (period == null) {
                return null;
            }
            return new DatePeriodResponse(
                toKstDate(period.startsAt()),
                toKstDate(period.endsAt())
            );
        }

        private static LocalDate toKstDate(Instant instant) {
            return instant == null ? null : instant.atZone(ZoneId.of("Asia/Seoul")).toLocalDate();
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
        RecruitmentPartListInfo.MyApplicationStatus status,
        Long draftFormResponseId,
        Long applicationId
    ) {
        public static MyApplicationResponse from(RecruitmentPartListInfo.MyApplicationInfo a) {
            return new MyApplicationResponse(a.status(), a.draftFormResponseId(), a.applicationId());
        }
    }
}
