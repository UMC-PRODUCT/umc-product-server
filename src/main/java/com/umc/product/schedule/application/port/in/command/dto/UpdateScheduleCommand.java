package com.umc.product.schedule.application.port.in.command.dto;

import com.umc.product.schedule.domain.enums.ScheduleType;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 일정 수정 Command
 */
public record UpdateScheduleCommand(
        Long scheduleId,
        String name,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        Boolean isAllDay,
        String locationName,
        String description,
        ScheduleType scheduleType
) {
    public UpdateScheduleCommand {
        Objects.requireNonNull(scheduleId, "scheduleId must not be null");

        if (startsAt != null && endsAt != null && startsAt.isAfter(endsAt)) {
            throw new IllegalArgumentException("시작 일시는 종료 일시보다 이전이어야 합니다");
        }
    }

    public static UpdateScheduleCommand of(
            Long scheduleId,
            String name,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            Boolean isAllDay,
            String locationName,
            String description,
            ScheduleType scheduleType
    ) {
        return new UpdateScheduleCommand(
                scheduleId,
                name,
                startsAt,
                endsAt,
                isAllDay,
                locationName,
                description,
                scheduleType
        );
    }
}
