package com.umc.product.schedule.adapter.in.web.dto.response;

import com.umc.product.schedule.application.port.in.dto.AttendanceRecordInfo;

public record AttendanceRecordResponse(
        Long id,
        Long attendanceSheetId,
        Long challengerId,
        String status,
        String memo
) {
    public static AttendanceRecordResponse from(AttendanceRecordInfo info) {
        return new AttendanceRecordResponse(
                info.id() != null ? info.id().id() : null,
                info.attendanceSheetId(),
                info.challengerId(),
                info.status().name(),
                info.memo()
        );
    }
}
