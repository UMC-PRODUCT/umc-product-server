package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentDraftInfo;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record RecruitmentDraftResponse(
        Long recruitmentId,
        String status,
        Long formId,

        String title,
        List<ChallengerPart> recruitmentParts,
        Integer maxPreferredPartCount,

        RecruitmentDraftScheduleResponse schedule,

        String noticeContent,

        Instant createdAt,
        Instant updatedAt
) {
    public record RecruitmentDraftScheduleResponse(
            Instant applyStartAt,
            Instant applyEndAt,
            Instant docResultAt,
            Instant interviewStartAt,
            Instant interviewEndAt,
            Instant finalResultAt,
            RecruitmentDraftInterviewTimeTableResponse interviewTimeTable
    ) {
    }

    public record RecruitmentDraftInterviewTimeTableResponse(
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

    public record TimesByDateResponse(
            LocalDate date,
            List<String> times
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

    private static RecruitmentDraftScheduleResponse toSchedule(RecruitmentDraftInfo.ScheduleInfo s) {
        if (s == null) {
            return null;
        }

        return new RecruitmentDraftScheduleResponse(
                s.applyStartAt(),
                s.applyEndAt(),
                s.docResultAt(),
                s.interviewStartAt(),
                s.interviewEndAt(),
                s.finalResultAt(),
                toInterviewTimeTable(s.interviewTimeTable())
        );
    }

    private static RecruitmentDraftInterviewTimeTableResponse toInterviewTimeTable(
            RecruitmentDraftInfo.InterviewTimeTableInfo t) {
        if (t == null) {
            return null;
        }

        return new RecruitmentDraftInterviewTimeTableResponse(
                new DateRangeResponse(t.dateRange().start(), t.dateRange().end()),
                new TimeRangeResponse(formatTime(t.timeRange().start()), formatTime(t.timeRange().end())),
                t.slotMinutes(),
                t.enabledByDate() == null ? null : t.enabledByDate().stream()
                        .map(x -> new TimesByDateResponse(
                                x.date(),
                                x.times() == null ? List.of()
                                        : x.times().stream().map(RecruitmentDraftResponse::formatTime).toList()
                        ))
                        .toList(),
                t.disabledByDate() == null ? null : t.disabledByDate().stream()
                        .map(x -> new TimesByDateResponse(
                                x.date(),
                                x.times() == null ? List.of()
                                        : x.times().stream().map(RecruitmentDraftResponse::formatTime).toList()
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
