package com.umc.product.schedule.application.port.in.attendance;

import com.umc.product.schedule.domain.vo.Location;
import java.time.LocalDateTime;
import java.util.Objects;

//출석 체크용
public record CheckAttendanceCommand(
        Long attendanceSheetId,
        Long challengerId,
        Location checkedLocation,
        LocalDateTime checkedAt
) {
    public CheckAttendanceCommand {
        Objects.requireNonNull(attendanceSheetId, "출석부 ID는 필수입니다");
        Objects.requireNonNull(challengerId, "챌린저 ID는 필수입니다");
        Objects.requireNonNull(checkedLocation, "체크 위치는 필수입니다");
        Objects.requireNonNull(checkedAt, "체크 시간은 필수입니다");
    }
}
