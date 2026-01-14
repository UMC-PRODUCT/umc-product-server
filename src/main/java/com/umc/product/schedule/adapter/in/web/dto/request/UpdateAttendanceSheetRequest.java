package com.umc.product.schedule.adapter.in.web.dto.request;

import com.umc.product.schedule.application.port.in.command.UpdateAttendanceSheetCommand;
import com.umc.product.schedule.domain.AttendanceSheet.AttendanceSheetId;
import com.umc.product.schedule.domain.vo.AttendanceWindow;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record UpdateAttendanceSheetRequest(
        @NotNull(message = "출석 시작 시간은 필수입니다")
        LocalDateTime startTime,

        @NotNull(message = "출석 종료 시간은 필수입니다")
        LocalDateTime endTime,

        @NotNull(message = "지각 인정 시간은 필수입니다")
        Integer lateThresholdMinutes,

        boolean requiresApproval
) {
    public UpdateAttendanceSheetCommand toCommand(Long sheetId) {
        AttendanceWindow window = AttendanceWindow.of(
                startTime,
                0,
                (int) java.time.Duration.between(startTime, endTime).toMinutes(),
                lateThresholdMinutes
        );
        return new UpdateAttendanceSheetCommand(new AttendanceSheetId(sheetId), window, requiresApproval);
    }
}
