package com.umc.product.schedule.application.port.in.command.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public record ProcessAttendanceCommand(
        @NotNull(message = "ScheduleAttendanceId는 null이 되면 안 됩니다.")
        Long attendanceId,
        @NotNull(message = "confirmerId는 null이 되면 안 됩니다.")
        Long confirmerId
) {
    public ProcessAttendanceCommand {
        Objects.requireNonNull(attendanceId, "attendanceId must not be null");
        Objects.requireNonNull(confirmerId, "confirmerId must not be null");
    }
}
