package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentPublishedInfo;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record RecruitmentPublishedResponse(
        Long recruitmentId,
        String status,     // PUBLISHED
        Long formId,

        String title,
        List<ChallengerPart> recruitmentParts,
        Integer maxPreferredPartCount,

        RecruitmentPublishedScheduleResponse schedule,

        String noticeContent,

        Instant createdAt,
        Instant updatedAt
) {
    public record RecruitmentPublishedScheduleResponse(
            Instant applyStartAt,
            Instant applyEndAt,
            Instant docResultAt,
            Instant interviewStartAt,
            Instant interviewEndAt,
            Instant finalResultAt,
            RecruitmentPublishedInterviewTimeTableResponse interviewTimeTable
    ) {
    }

    public record RecruitmentPublishedInterviewTimeTableResponse(
            DateRangeResponse dateRange,
            TimeRangeResponse timeRange,
            Integer slotMinutes,
            List<TimesByDateResponse> enabledByDate,
            List<TimesByDateResponse> disabledByDate
    ) {
    }

    public record DateRangeResponse(LocalDate start, LocalDate end) {
    }

    public record TimeRangeResponse(String start, String end) {
    }

    public record TimesByDateResponse(LocalDate date, List<String> times) {
    }

    public static RecruitmentPublishedResponse from(RecruitmentPublishedInfo info) {
        return new RecruitmentPublishedResponse(
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

    private static RecruitmentPublishedScheduleResponse toSchedule(RecruitmentPublishedInfo.ScheduleInfo s) {
        if (s == null) {
            return null;
        }

        return new RecruitmentPublishedScheduleResponse(
                s.applyStartAt(),
                s.applyEndAt(),
                s.docResultAt(),
                s.interviewStartAt(),
                s.interviewEndAt(),
                s.finalResultAt(),
                toInterviewTimeTable(s.interviewTimeTable())
        );
    }

    private static RecruitmentPublishedInterviewTimeTableResponse toInterviewTimeTable(
            RecruitmentPublishedInfo.InterviewTimeTableInfo t
    ) {
        if (t == null) {
            return null;
        }

        return new RecruitmentPublishedInterviewTimeTableResponse(
                new DateRangeResponse(t.dateRange().start(), t.dateRange().end()),
                new TimeRangeResponse(formatTime(t.timeRange().start()), formatTime(t.timeRange().end())),
                t.slotMinutes(),
                t.enabledByDate() == null ? null : t.enabledByDate().stream()
                        .map(x -> new TimesByDateResponse(
                                x.date(),
                                x.times() == null ? List.of()
                                        : x.times().stream().map(RecruitmentPublishedResponse::formatTime).toList()
                        ))
                        .toList(),
                t.disabledByDate() == null ? null : t.disabledByDate().stream()
                        .map(x -> new TimesByDateResponse(
                                x.date(),
                                x.times() == null ? List.of()
                                        : x.times().stream().map(RecruitmentPublishedResponse::formatTime).toList()
                        ))
                        .toList()
        );
    }

    private static String formatTime(LocalTime t) {
        if (t == null) {
            return null;
        }
        return t.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}