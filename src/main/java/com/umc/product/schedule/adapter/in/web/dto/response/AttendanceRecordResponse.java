package com.umc.product.schedule.adapter.in.web.dto.response;

public record AttendanceRecordResponse(
        Long id,
        Long attendanceSheetId,
        Long challengerId,
        String status,
        String memo
) {
}
