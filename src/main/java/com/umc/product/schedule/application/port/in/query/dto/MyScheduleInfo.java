package com.umc.product.schedule.application.port.in.query.dto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record MyScheduleInfo(
    Long scheduleId,
    String name,
    LocalDateTime startsAt,
    LocalDateTime endsAt,
    String status,
    long dDay
) {

    public static MyScheduleInfo of(
        Long scheduleId,
        String name,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        LocalDateTime now
    ) {
        String status = resolveStatus(startsAt, endsAt, now);
        long dDay = ChronoUnit.DAYS.between(now.toLocalDate(), startsAt.toLocalDate());

        return new MyScheduleInfo(scheduleId, name, startsAt, endsAt, status, dDay);
    }

    private static String resolveStatus(LocalDateTime startsAt, LocalDateTime endsAt, LocalDateTime now) {
        if (now.isAfter(endsAt)) {
            return "종료됨";
        }
        if (now.isAfter(startsAt) && now.isBefore(endsAt)) {
            return "진행 중";
        }
        return "참여 예정";
    }
}
