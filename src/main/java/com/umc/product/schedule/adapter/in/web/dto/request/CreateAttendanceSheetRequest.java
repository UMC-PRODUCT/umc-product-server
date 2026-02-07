package com.umc.product.schedule.adapter.in.web.dto.request;

import static com.umc.product.schedule.domain.ScheduleConstants.KST;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.schedule.application.port.in.command.dto.CreateAttendanceSheetCommand;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import com.umc.product.schedule.domain.vo.AttendanceWindow;
import java.time.Instant;
import java.util.List;

public record CreateAttendanceSheetRequest(
    Long gisuId,
    Instant startTime,
    Instant endTime,
    boolean requiresApproval,
    List<Long> participantMemberIds
) {
    private static final int DEFAULT_LATE_THRESHOLD_MINUTES = 10;

    public CreateAttendanceSheetRequest {
        if (gisuId == null) {
            throw new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.GISU_ID_REQUIRED);
        }

        if (startTime == null || endTime == null) {
            throw new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.INVALID_TIME_RANGE);
        }

        if (startTime.isAfter(endTime)) {
            throw new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.INVALID_TIME_RANGE);
        }
    }

    public CreateAttendanceSheetCommand toCommand(Long scheduleId) {
        AttendanceWindow window = AttendanceWindow.from(
            startTime,
            endTime,
            DEFAULT_LATE_THRESHOLD_MINUTES
        );
        return new CreateAttendanceSheetCommand(scheduleId, gisuId, window, requiresApproval, participantMemberIds);
    }
}
