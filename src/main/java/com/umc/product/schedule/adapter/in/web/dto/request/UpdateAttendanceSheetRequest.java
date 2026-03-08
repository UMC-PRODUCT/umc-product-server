package com.umc.product.schedule.adapter.in.web.dto.request;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.schedule.application.port.in.command.dto.UpdateAttendanceSheetCommand;
import com.umc.product.schedule.domain.AttendanceSheet.AttendanceSheetId;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import com.umc.product.schedule.domain.vo.AttendanceWindow;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "출석부 수정 요청")
public record UpdateAttendanceSheetRequest(
    @Schema(description = "출석 시작 시간 (UTC ISO8601. 예: 2026-03-16T01:00:00Z)", example = "2026-03-16T01:00:00Z")
    Instant startTime,

    @Schema(description = "출석 종료 시간 (UTC ISO8601. 예: 2026-03-16T03:00:00Z)", example = "2026-03-16T03:00:00Z")
    Instant endTime,

    @Schema(description = "지각 기준 시간 (분)", example = "10")
    Integer lateThresholdMinutes,

    @Schema(description = "승인 필요 여부", example = "true")
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
