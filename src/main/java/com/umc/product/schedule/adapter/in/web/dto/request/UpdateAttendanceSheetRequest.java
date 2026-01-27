package com.umc.product.schedule.adapter.in.web.dto.request;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.schedule.application.port.in.command.dto.UpdateAttendanceSheetCommand;
import com.umc.product.schedule.domain.AttendanceSheet.AttendanceSheetId;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import com.umc.product.schedule.domain.vo.AttendanceWindow;
import java.time.LocalDateTime;

public record UpdateAttendanceSheetRequest(
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer lateThresholdMinutes,
        boolean requiresApproval
) {
    public UpdateAttendanceSheetRequest {
        if (startTime == null || endTime == null) {
            throw new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.INVALID_TIME_RANGE);
        }

        if (lateThresholdMinutes == null) {
            throw new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.INVALID_LATE_THRESHOLD);
        }

        if (startTime.isAfter(endTime)) {
            throw new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.INVALID_TIME_RANGE);
        }
    }

    public UpdateAttendanceSheetCommand toCommand(Long sheetId) {
        AttendanceWindow window = AttendanceWindow.from(
                startTime,
                endTime,
                lateThresholdMinutes
        );
        return new UpdateAttendanceSheetCommand(new AttendanceSheetId(sheetId), window, requiresApproval);
    }
}
