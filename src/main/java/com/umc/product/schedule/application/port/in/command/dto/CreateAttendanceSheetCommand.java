package com.umc.product.schedule.application.port.in.command.dto;

import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.vo.AttendanceWindow;
import java.util.Objects;

/**
 * 출석부 생성 Command
 */
public record CreateAttendanceSheetCommand(
    Long scheduleId,
    AttendanceWindow window,
    boolean requiresApproval
) {
    public CreateAttendanceSheetCommand {
        Objects.requireNonNull(scheduleId, "일정 ID는 필수입니다");
        Objects.requireNonNull(window, "출석 시간대는 필수입니다");
    }

    public AttendanceSheet toEntity(Schedule schedule) {
        return AttendanceSheet.builder()
            .scheduleId(schedule.getId())
            .window(window)
            .requiresApproval(requiresApproval)
            .build();
    }
}
