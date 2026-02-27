package com.umc.product.schedule.application.port.in.command.dto;

import java.time.Instant;
import java.util.Objects;

/**
 * 출석 체크 Command
 */
public record CheckAttendanceCommand(
    Long attendanceSheetId,
    Long memberId,
    Instant checkedAt,
    Double latitude,
    Double longitude,
    Boolean locationVerified
) {
    public CheckAttendanceCommand {
        Objects.requireNonNull(attendanceSheetId, "출석부 ID는 필수입니다");
        Objects.requireNonNull(memberId, "멤버 ID는 필수입니다");
        Objects.requireNonNull(checkedAt, "체크 시간은 필수입니다");
        Objects.requireNonNull(locationVerified, "위치 인증 여부는 필수입니다");
    }
}
