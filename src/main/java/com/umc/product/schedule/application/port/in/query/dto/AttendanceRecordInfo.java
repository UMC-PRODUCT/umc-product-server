package com.umc.product.schedule.application.port.in.query.dto;

import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceRecord.AttendanceRecordId;
import com.umc.product.schedule.domain.enums.AttendanceStatus;

/**
 * 출석 기록 정보 DTO
 */
public record AttendanceRecordInfo(
        AttendanceRecordId id,
        Long attendanceSheetId,
        Long challengerId,
        AttendanceStatus status,
        String memo
) {
    public static AttendanceRecordInfo from(AttendanceRecord record) {
        return new AttendanceRecordInfo(
                record.getAttendanceRecordId(),
                record.getAttendanceSheetId(),
                record.getChallengerId(),
                record.getStatus(),
                record.getMemo()
        );
    }
}
