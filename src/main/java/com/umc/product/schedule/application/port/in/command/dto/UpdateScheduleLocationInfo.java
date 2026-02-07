package com.umc.product.schedule.application.port.in.command.dto;

import org.locationtech.jts.geom.Point;

public record UpdateScheduleLocationInfo(
    Long scheduleId,
    String locationName,
    Double latitude,
    Double longitude
) {
    public static UpdateScheduleLocationInfo of(Long scheduleId, String locationName, Point location) {
        return new UpdateScheduleLocationInfo(
            scheduleId,
            locationName,
            location.getY(),
            location.getX()
        );
    }
}
