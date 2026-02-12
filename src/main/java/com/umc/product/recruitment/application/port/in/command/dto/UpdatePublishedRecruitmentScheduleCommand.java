package com.umc.product.recruitment.application.port.in.command.dto;

import java.time.Instant;

public record UpdatePublishedRecruitmentScheduleCommand(
    Long memberId,
    Long recruitmentId,
    SchedulePatch schedule,
    Integer slotMinutes
) {
    public record SchedulePatch(
        Instant applyStartAt,
        Instant applyEndAt,
        Instant docResultAt,
        Instant interviewStartAt,
        Instant interviewEndAt,
        Instant finalResultAt
    ) {
    }
}
