package com.umc.product.schedule.adapter.in.web.dto.request;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.schedule.application.port.in.command.dto.CheckAttendanceCommand;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import java.time.LocalDateTime;

public record CheckAttendanceRequest(
        Long attendanceSheetId
) {
    public CheckAttendanceRequest {
        if (attendanceSheetId == null) {
            throw new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.ATTENDANCE_SHEET_NOT_FOUND);
        }
    }

    public CheckAttendanceCommand toCommand(Long challengerId) {
        return new CheckAttendanceCommand(attendanceSheetId, challengerId, LocalDateTime.now());
    }
}
