package com.umc.product.schedule.application.port.in.query.dto;

import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.time.LocalDateTime;

public record PendingAttendanceInfo(
        Long attendanceId,
        Long challengerId,
        String memberName,
        String nickname,
        String schoolName,
        AttendanceStatus status,
        String reason,
        LocalDateTime requestedAt
) {
    /**
     * AttendanceRecord와 멤버 정보로 생성 memberName, nickname, schoolName은 다른 도메인(member, organization)에서 조회해야 함
     */
    public static PendingAttendanceInfo from(
            AttendanceRecord record,
            String memberName,
            String nickname,
            String schoolName
    ) {
        return new PendingAttendanceInfo(
                record.getId(),
                record.getMemberId(),
                memberName,
                nickname,
                schoolName,
                record.getStatus(),
                record.getMemo(),
                record.getCheckedAt()
        );
    }
}
