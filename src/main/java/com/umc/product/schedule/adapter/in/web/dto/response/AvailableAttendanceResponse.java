package com.umc.product.schedule.adapter.in.web.dto.response;

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
}
