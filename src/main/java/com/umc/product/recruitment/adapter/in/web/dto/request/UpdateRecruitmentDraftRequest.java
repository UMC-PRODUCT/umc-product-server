package com.umc.product.recruitment.adapter.in.web.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateRecruitmentDraftCommand;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record UpdateRecruitmentDraftRequest(
        String title,
        List<ChallengerPart> recruitmentParts,
        Integer maxPreferredPartCount,
        ScheduleRequest schedule,
        String noticeContent
) {
    public record ScheduleRequest(
            Instant applyStartAt,
            Instant applyEndAt,
            Instant docResultAt,
            Instant interviewStartAt,
            Instant interviewEndAt,
            Instant finalResultAt,

            InterviewTimeTableRequest interviewTimeTable
    ) {
    }

    public record InterviewTimeTableRequest(
            DateRangeRequest dateRange,
            @Schema(example = "{\"start\":\"09:00\",\"end\":\"23:00\"}")
            TimeRangeRequest timeRange,
            Integer slotMinutes,
            List<EnabledTimesByDateRequest> enabledByDate
    ) {
    }

    public record DateRangeRequest(
            LocalDate start,
            LocalDate end
    ) {
    }

    public record TimeRangeRequest(
            @JsonFormat(pattern = "HH:mm") LocalTime start,
            @JsonFormat(pattern = "HH:mm") LocalTime end
    ) {
    }

    public record EnabledTimesByDateRequest(
            LocalDate date,
            @ArraySchema(
                    schema = @Schema(type = "string", format = "time", example = "09:00"),
                    arraySchema = @Schema(example = "[\"09:00\",\"09:30\",\"10:00\"]")
            )
            @JsonFormat(pattern = "HH:mm")
            List<LocalTime> times
    ) {
    }

    public static UpdateRecruitmentDraftRequest empty() {
        return new UpdateRecruitmentDraftRequest(null, null, null, null, null);
    }

    public UpdateRecruitmentDraftCommand toCommand(Long recruitmentId, Long requesterMemberId) {
        return new UpdateRecruitmentDraftCommand(
                recruitmentId,
                requesterMemberId,
                title,
                recruitmentParts,
                maxPreferredPartCount,
                toScheduleCommand(schedule),
                noticeContent
        );
    }

    private static UpdateRecruitmentDraftCommand.ScheduleCommand toScheduleCommand(ScheduleRequest s) {
        if (s == null) {
            return null;
        }

        return new UpdateRecruitmentDraftCommand.ScheduleCommand(
                s.applyStartAt(),
                s.applyEndAt(),
                s.docResultAt(),
                s.interviewStartAt(),
                s.interviewEndAt(),
                s.finalResultAt(),
                toInterviewTimeTableCommand(s.interviewTimeTable())
        );
    }

    private static UpdateRecruitmentDraftCommand.InterviewTimeTableCommand toInterviewTimeTableCommand(
            InterviewTimeTableRequest t
    ) {
        if (t == null) {
            return null;
        }

        return new UpdateRecruitmentDraftCommand.InterviewTimeTableCommand(
                new UpdateRecruitmentDraftCommand.DateRangeCommand(t.dateRange().start(), t.dateRange().end()),
                new UpdateRecruitmentDraftCommand.TimeRangeCommand(t.timeRange().start(), t.timeRange().end()),
                t.slotMinutes(),
                t.enabledByDate() == null ? null : t.enabledByDate().stream()
                        .map(x -> new UpdateRecruitmentDraftCommand.EnabledTimesByDateCommand(x.date(), x.times()))
                        .toList()
        );
    }
}
