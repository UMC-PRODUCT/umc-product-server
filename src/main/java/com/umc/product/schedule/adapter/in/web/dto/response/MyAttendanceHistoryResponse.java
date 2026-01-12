package com.umc.product.schedule.adapter.in.web.dto.response;

import com.umc.product.schedule.application.port.in.query.dto.MyAttendanceHistoryInfo;

public record MyAttendanceHistoryResponse(
        Long attendanceId,
        Long scheduleId,
        String scheduleName,
        String weekDisplay,
        String dateDisplay,
        String status,
        String statusDisplay
) {
    public static MyAttendanceHistoryResponse from(MyAttendanceHistoryInfo info) {
        return new MyAttendanceHistoryResponse(
                info.attendanceId(),
                info.scheduleId(),
                info.scheduleName(),
                info.weekDisplay(),
                info.dateDisplay(),
                info.status().name(),
                info.statusDisplay()
        );
    }
}
