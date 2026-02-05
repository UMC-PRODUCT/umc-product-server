package com.umc.product.schedule.application.port.in.query.dto;

import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import java.time.Instant;
import java.util.Set;

public record ScheduleDetailInfo(
    Long scheduleId,
    String name,
    String description,
    Set<ScheduleTag> tags,
    Instant startsAt,
    Instant endsAt,
    boolean isAllDay,
    String locationName,
    Double latitude,
    Double longitude,
    String status,
    boolean requiresAttendanceApproval
) {

    public static ScheduleDetailInfo from(Schedule schedule, Instant now, boolean requiresAttendanceApproval) {
        Double lat = schedule.getLocation() != null ? schedule.getLocation().getY() : null;
        Double lng = schedule.getLocation() != null ? schedule.getLocation().getX() : null;

        return new ScheduleDetailInfo(
            schedule.getId(),
            schedule.getName(),
            schedule.getDescription(),
            Set.copyOf(schedule.getTags()),
            schedule.getStartsAt(),
            schedule.getEndsAt(),
            schedule.isAllDay(),
            schedule.getLocationName(),
            lat,
            lng,
            schedule.resolveStatus(now),
            requiresAttendanceApproval
        );
    }
}
