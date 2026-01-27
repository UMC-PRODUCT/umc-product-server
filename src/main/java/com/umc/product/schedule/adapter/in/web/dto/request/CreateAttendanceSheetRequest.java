package com.umc.product.schedule.adapter.in.web.dto.request;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.schedule.application.port.in.command.dto.CreateAttendanceSheetCommand;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import com.umc.product.schedule.domain.vo.AttendanceWindow;
import java.time.LocalDateTime;

public record CreateAttendanceSheetRequest(
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer lateThresholdMinutes,
        boolean requiresApproval
) {
    public CreateAttendanceSheetRequest {
        if (startTime == null || endTime == null) {
            throw new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.INVALID_TIME_RANGE);
        }

        if (lateThresholdMinutes == null) {
            throw new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.INVALID_ATTENDANCE_STATUS);
        }

        if (startTime.isAfter(endTime)) {
            throw new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.INVALID_TIME_RANGE);
        }
    }

    public CreateAttendanceSheetCommand toCommand(Long scheduleId) {
        AttendanceWindow window = AttendanceWindow.from(
                startTime,
                endTime,
                lateThresholdMinutes
        );
        return new CreateAttendanceSheetCommand(scheduleId, window, requiresApproval);
    }
}
