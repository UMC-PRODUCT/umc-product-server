package com.umc.product.schedule.application.port.in.command.dto;

import java.util.Objects;
import org.locationtech.jts.geom.Point;

/**
 * 일정 출석체크 위치 변경 Command
 */
public record UpdateScheduleLocationCommand(
    Long scheduleId,
    String locationName,
    Point location
) {
    public UpdateScheduleLocationCommand {
        Objects.requireNonNull(scheduleId, "scheduleId must not be null");
        Objects.requireNonNull(locationName, "locationName must not be null");
        Objects.requireNonNull(location, "location must not be null");
    }

    public static UpdateScheduleLocationCommand of(
        Long scheduleId,
        String locationName,
        Point location
    ) {
        return new UpdateScheduleLocationCommand(
            scheduleId,
            locationName,
            location
        );
    }
}
