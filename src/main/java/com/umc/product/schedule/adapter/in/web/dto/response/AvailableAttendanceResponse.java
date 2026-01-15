package com.umc.product.schedule.adapter.in.web.dto.response;

import com.umc.product.schedule.application.port.in.query.dto.AvailableAttendanceInfo;
import java.time.LocalTime;

public record AvailableAttendanceResponse(
        Long scheduleId,
        String scheduleName,
        String scheduleType,
        LocalTime startTime,
        LocalTime endTime,
        Long sheetId,
        Long recordId,
        String status,
        String statusDisplay
) {
    public static AvailableAttendanceResponse from(AvailableAttendanceInfo info) {
        return new AvailableAttendanceResponse(
                info.scheduleId(),
                info.scheduleName(),
                info.scheduleType().name(),
                info.startTime(),
                info.endTime(),
                info.sheetId(),
                info.recordId(),
                info.status().name(),
                info.statusDisplay()
        );
    }
}
