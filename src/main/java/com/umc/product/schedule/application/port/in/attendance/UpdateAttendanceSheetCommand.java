package com.umc.product.schedule.application.port.in.attendance;

import com.umc.product.schedule.domain.AttendanceSheet.AttendanceSheetId;
import com.umc.product.schedule.domain.vo.AttendanceWindow;
import com.umc.product.schedule.domain.vo.Location;
import com.umc.product.schedule.domain.vo.LocationRange;
import java.util.Objects;

//출석부 수정
public record UpdateAttendanceSheetCommand(
        AttendanceSheetId sheetId,
        Location location,
        LocationRange range,
        AttendanceWindow window,
        boolean requiresApproval
) {
    public UpdateAttendanceSheetCommand {
        Objects.requireNonNull(sheetId, "출석부 ID는 필수입니다");
        Objects.requireNonNull(location, "위치는 필수입니다");
        Objects.requireNonNull(range, "위치 범위는 필수입니다");
        Objects.requireNonNull(window, "출석 시간대는 필수입니다");
    }
}
