package com.umc.product.schedule.adapter.in.web.dto.response;

import com.umc.product.schedule.application.port.in.query.dto.ScheduleWithStatsInfo;
import java.time.LocalTime;

public record ScheduleListResponse(
        Long scheduleId,
        String name,
        String type,
        String status,
        String date,
        LocalTime startTime,
        LocalTime endTime,
        String locationName,
        Integer totalCount,
        Integer presentCount,
        Integer pendingCount,
        Double attendanceRate
) {
    public static ScheduleListResponse from(ScheduleWithStatsInfo info) {
        return new ScheduleListResponse(
                info.scheduleId(),
                info.name(),
                info.type().name(),
                info.status(),
                info.date(),
                info.startTime(),
                info.endTime(),
                info.locationName(),
                info.totalCount(),
                info.presentCount(),
                info.pendingCount(),
                info.attendanceRate()
        );
    }
}
