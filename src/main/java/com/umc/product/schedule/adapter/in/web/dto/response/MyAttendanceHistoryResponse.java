package com.umc.product.schedule.adapter.in.web.dto.response;

public record MyAttendanceHistoryResponse(
        Long attendanceId,
        Long scheduleId,
        String scheduleName,
        String weekDisplay,
        String dateDisplay,
        String status,
        String statusDisplay
) {
}
