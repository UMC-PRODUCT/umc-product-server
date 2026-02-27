package com.umc.product.recruitment.application.port.in.command.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record UpdateRecruitmentDraftCommand(
    Long recruitmentId,
    Long requesterMemberId,
    String title,
    List<ChallengerPart> recruitmentParts,
    Integer maxPreferredPartCount,
    ScheduleCommand schedule,
    String noticeContent
) {

    public record ScheduleCommand(
        Instant applyStartAt,
        Instant applyEndAt,
        Instant docResultAt,
        Instant interviewStartAt,
        Instant interviewEndAt,
        Instant finalResultAt,
        InterviewTimeTableCommand interviewTimeTable
    ) {
    }

    public record InterviewTimeTableCommand(
        DateRangeCommand dateRange,
        TimeRangeCommand timeRange,
        Integer slotMinutes,
        List<EnabledTimesByDateCommand> enabledByDate
    ) {
    }

    public record DateRangeCommand(LocalDate start, LocalDate end) {
    }

    public record TimeRangeCommand(LocalTime start, LocalTime end) {
    }

    public record EnabledTimesByDateCommand(
        LocalDate date,
        List<LocalTime> times
    ) {
    }

}
