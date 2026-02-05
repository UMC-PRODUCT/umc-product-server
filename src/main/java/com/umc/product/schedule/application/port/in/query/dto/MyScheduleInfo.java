package com.umc.product.schedule.application.port.in.query.dto;

import java.time.Instant;

public record MyScheduleInfo(
    Long scheduleId,
    String name,
    Instant startsAt,
    Instant endsAt,
    String status
) {

    public static MyScheduleInfo of(
        Long scheduleId,
        String name,
        Instant startsAt,
        Instant endsAt,
        Instant now
    ) {
        String status = resolveStatus(startsAt, endsAt, now);

        return new MyScheduleInfo(scheduleId, name, startsAt, endsAt, status);
    }

    private static String resolveStatus(Instant startsAt, Instant endsAt, Instant now) {
        if (now.isAfter(endsAt)) {
            return "종료됨";
        }
        if (now.isAfter(startsAt) && now.isBefore(endsAt)) {
            return "진행 중";
        }
        return "참여 예정";
    }
}
