package com.umc.product.schedule.application.port.in.command.dto;

import com.umc.product.schedule.domain.AttendanceSheet.AttendanceSheetId;
import com.umc.product.schedule.domain.vo.AttendanceWindow;
import java.util.Objects;

/**
 * 출석부 수정 Command
 */
public record UpdateAttendanceSheetCommand(
        AttendanceSheetId sheetId,
        AttendanceWindow window,
        boolean requiresApproval
) {
    public UpdateAttendanceSheetCommand {
        Objects.requireNonNull(sheetId, "출석부 ID는 필수입니다");
        Objects.requireNonNull(window, "출석 시간대는 필수입니다");
    }
}
