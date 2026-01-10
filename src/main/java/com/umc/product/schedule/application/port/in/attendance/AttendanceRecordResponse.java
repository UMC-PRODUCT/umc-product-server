package com.umc.product.schedule.application.port.in.attendance;

import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceRecord.AttendanceRecordId;
import com.umc.product.schedule.domain.AttendanceStatus;
import com.umc.product.schedule.domain.vo.Location;
import java.time.LocalDateTime;

//출석 기록 응답 DTO
public record AttendanceRecordResponse(
        AttendanceRecordId id,
        Long attendanceSheetId,
        Long challengerId,
        AttendanceStatus status,
        Location checkedLocation,
        LocalDateTime checkedAt,
        String memo
) {
    public static AttendanceRecordResponse from(AttendanceRecord record) {
        return new AttendanceRecordResponse(
                record.getAttendanceRecordId(),
                record.getAttendanceSheetId(),
                record.getChallengerId(),
                record.getStatus(),
                record.getCheckedLocation(),
                record.getCheckedAt(),
                record.getMemo()
        );
    }
}
