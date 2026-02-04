package com.umc.product.schedule.adapter.in.web.dto.response;

import java.time.LocalTime;
import java.util.List;

public record AvailableAttendanceResponse(
    Long scheduleId,
    String scheduleName,
    List<String> tags,
    LocalTime startTime,
    LocalTime endTime,
    Long sheetId,
    Long recordId,
    String status,
    String statusDisplay
) {
}
