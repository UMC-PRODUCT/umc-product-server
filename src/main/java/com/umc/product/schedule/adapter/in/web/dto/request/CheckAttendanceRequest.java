package com.umc.product.schedule.adapter.in.web.dto.request;

import com.umc.product.schedule.application.port.in.command.CheckAttendanceCommand;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CheckAttendanceRequest(
        @NotNull(message = "출석부 ID는 필수입니다")
        Long attendanceSheetId
) {
    public CheckAttendanceCommand toCommand(Long challengerId) {
        return new CheckAttendanceCommand(attendanceSheetId, challengerId, LocalDateTime.now());
    }
}
