package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentDraftInfo;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record RecruitmentDraftResponse(
        Long recruitmentId,
        String status,
        Long formId,

        String title,
        List<ChallengerPart> recruitmentParts,
        Integer maxPreferredPartCount,

        ScheduleResponse schedule,

        String noticeContent,

        Instant createdAt,
        Instant updatedAt
) {
    public record ScheduleResponse(
            Instant applyStartAt,
            Instant applyEndAt,
            Instant docResultAt,
            Instant interviewStartAt,
            Instant interviewEndAt,
            Instant finalResultAt,
            InterviewTimeTableResponse interviewTimeTable
    ) {
    }

    public record InterviewTimeTableResponse(
            DateRangeResponse dateRange,
            TimeRangeResponse timeRange,
            Integer slotMinutes,
            List<TimesByDateResponse> enabledByDate,
            List<TimesByDateResponse> disabledByDate
    ) {
    }

    public record DateRangeResponse(LocalDate start, LocalDate end) {
    }

    public record TimeRangeResponse(LocalTime start, LocalTime end) {
    }

    public record TimesByDateResponse(
            LocalDate date,
            List<LocalTime> times
    ) {
    }

    public static RecruitmentDraftResponse from(RecruitmentDraftInfo info) {
        return new RecruitmentDraftResponse(
                info.recruitmentId(),
                info.status(),
                info.formId(),
                info.title(),
                info.recruitmentParts(),
                info.maxPreferredPartCount(),
                toSchedule(info.schedule()),
                info.noticeContent(),
                info.createdAt(),
                info.updatedAt()
        );
    }

    private static ScheduleResponse toSchedule(RecruitmentDraftInfo.ScheduleInfo s) {
        if (s == null) {
            return null;
        }

        return new ScheduleResponse(
                s.applyStartAt(),
                s.applyEndAt(),
                s.docResultAt(),
                s.interviewStartAt(),
                s.interviewEndAt(),
                s.finalResultAt(),
                toInterviewTimeTable(s.interviewTimeTable())
        );
    }

    private static InterviewTimeTableResponse toInterviewTimeTable(RecruitmentDraftInfo.InterviewTimeTableInfo t) {
        if (t == null) {
            return null;
        }

        return new InterviewTimeTableResponse(
                new DateRangeResponse(t.dateRange().start(), t.dateRange().end()),
                new TimeRangeResponse(t.timeRange().start(), t.timeRange().end()),
                t.slotMinutes(),
                t.enabledByDate() == null ? null : t.enabledByDate().stream()
                        .map(x -> new TimesByDateResponse(x.date(), x.times()))
                        .toList(),
                t.disabledByDate() == null ? null : t.disabledByDate().stream()
                        .map(x -> new TimesByDateResponse(x.date(), x.times()))
                        .toList()
        );
    }
}
