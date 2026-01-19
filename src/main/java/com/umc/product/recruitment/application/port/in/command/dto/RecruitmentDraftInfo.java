package com.umc.product.recruitment.application.port.in.command.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record RecruitmentDraftInfo(
        Long recruitmentId,
        String status,
        Long formId,

        String title,
        List<ChallengerPart> recruitmentParts,
        Integer maxPreferredPartCount,

        ScheduleInfo schedule,

        String noticeContent,

        Instant createdAt,
        Instant updatedAt
) {
    public record ScheduleInfo(
            Instant applyStartAt,
            Instant applyEndAt,
            Instant docResultAt,
            Instant interviewStartAt,
            Instant interviewEndAt,
            Instant finalResultAt,
            InterviewTimeTableInfo interviewTimeTable
    ) {
    }

    public record InterviewTimeTableInfo(
            DateRangeInfo dateRange,
            TimeRangeInfo timeRange,
            Integer slotMinutes,
            List<TimesByDateInfo> enabledByDate,
            List<TimesByDateInfo> disabledByDate
    ) {
    }

    public record DateRangeInfo(LocalDate start, LocalDate end) {
    }

    public record TimeRangeInfo(LocalTime start, LocalTime end) {
    }

    public record TimesByDateInfo(
            LocalDate date,
            List<LocalTime> times
    ) {
    }
}
