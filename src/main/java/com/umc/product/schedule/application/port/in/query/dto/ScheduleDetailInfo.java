package com.umc.product.schedule.application.port.in.query.dto;

import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

public record ScheduleDetailInfo(
    Long scheduleId,
    String name,
    String description,
    Set<ScheduleTag> tags,
    LocalDateTime startsAt,
    LocalDateTime endsAt,
    boolean isAllDay,
    String locationName,
    Double latitude,
    Double longitude,
    String status,
    long dDay,
    boolean requiresAttendanceApproval
) {

    public static ScheduleDetailInfo from(Schedule schedule, LocalDateTime now, boolean requiresAttendanceApproval) {
        Double lat = schedule.getLocation() != null ? schedule.getLocation().getY() : null;
        Double lng = schedule.getLocation() != null ? schedule.getLocation().getX() : null;
        long dDay = ChronoUnit.DAYS.between(now.toLocalDate(), schedule.getStartsAt().toLocalDate());

        return new ScheduleDetailInfo(
            schedule.getId(),
            schedule.getName(),
            schedule.getDescription(),
            schedule.getTags(),
            schedule.getStartsAt(),
            schedule.getEndsAt(),
            schedule.isAllDay(),
            schedule.getLocationName(),
            lat,
            lng,
            schedule.resolveStatus(now),
            dDay,
            requiresAttendanceApproval
        );
    }
}
