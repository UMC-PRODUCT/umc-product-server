package com.umc.product.schedule.application.port.in.command.dto;

import com.umc.product.schedule.domain.enums.ScheduleTag;
import com.umc.product.schedule.domain.exception.ScheduleDomainException;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import org.locationtech.jts.geom.Point;

/**
 * 일정 수정 Command
 */
public record UpdateScheduleCommand(
    Long scheduleId,
    String name,
    Instant startsAt,
    Instant endsAt,
    Boolean isAllDay,
    String locationName,
    Point location,
    String description,
    Set<ScheduleTag> tags
) {
    public UpdateScheduleCommand {
        Objects.requireNonNull(scheduleId, "scheduleId must not be null");

        if (startsAt != null && endsAt != null && startsAt.isAfter(endsAt)) {
            throw new ScheduleDomainException(ScheduleErrorCode.INVALID_TIME_RANGE);
        }
    }

    public static UpdateScheduleCommand of(
        Long scheduleId,
        String name,
        Instant startsAt,
        Instant endsAt,
        Boolean isAllDay,
        String locationName,
        Point location,
        String description,
        Set<ScheduleTag> tags
    ) {
        return new UpdateScheduleCommand(
            scheduleId,
            name,
            startsAt,
            endsAt,
            isAllDay,
            locationName,
            location,
            description,
            tags
        );
    }
}
