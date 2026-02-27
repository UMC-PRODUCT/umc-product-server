package com.umc.product.schedule.application.port.in.query.dto;

import com.umc.product.schedule.domain.ScheduleConstants;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public record MyScheduleInfo(
    Long scheduleId,
    String name,
    Instant startsAt,
    Instant endsAt,
    String status,
    long dDay
) {

    public static MyScheduleInfo of(
        Long scheduleId,
        String name,
        Instant startsAt,
        Instant endsAt,
        Instant now
    ) {
        String status = resolveStatus(startsAt, endsAt, now);
        long dDay = ChronoUnit.DAYS.between(
            now.atZone(ScheduleConstants.KST).toLocalDate(),
            startsAt.atZone(ScheduleConstants.KST).toLocalDate()
        );

        return new MyScheduleInfo(scheduleId, name, startsAt, endsAt, status, dDay);
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
