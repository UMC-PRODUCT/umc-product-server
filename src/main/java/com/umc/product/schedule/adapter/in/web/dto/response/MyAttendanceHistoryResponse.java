package com.umc.product.schedule.adapter.in.web.dto.response;

import java.time.LocalDateTime;

public record MyAttendanceHistoryResponse(
        Long attendanceId,
        Long scheduleId,
        String scheduleName,
        LocalDateTime scheduledAt,
        String dateDisplay,
        String status,
        String statusDisplay
) {
}
