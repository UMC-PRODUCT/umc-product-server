package com.umc.product.schedule.application.port.in.query.dto;

import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.time.LocalDateTime;

public record PendingAttendanceInfo(
    Long attendanceId,
    Long memberId,
    String memberName,
    String nickname,
    String schoolName,
    AttendanceStatus status,
    String reason,
    LocalDateTime requestedAt
) {
    public static PendingAttendanceInfo of(
        AttendanceRecord record,
        Long memberId,
        String memberName,
        String nickname,
        String schoolName
    ) {
        return new PendingAttendanceInfo(
            record.getId(),
            memberId,
            memberName,
            nickname,
            schoolName,
            record.getStatus(),
            record.getMemo(),
            record.getCheckedAt()
        );
    }
}
