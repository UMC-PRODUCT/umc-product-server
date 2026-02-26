package com.umc.product.schedule.adapter.in.web.dto.request;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.schedule.application.port.in.command.dto.CreateAttendanceSheetCommand;
import com.umc.product.schedule.domain.ScheduleConstants;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import com.umc.product.schedule.domain.vo.AttendanceWindow;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "출석부 생성 요청")
public record CreateAttendanceSheetRequest(
    @Schema(description = "기수 ID", example = "1")
    Long gisuId,

    @Schema(description = "출석 시작 시간 (KST 기준 LocalDateTime, 타임존 없이 전송. 예: 2026-03-16T10:00:00)", example = "2026-03-16T10:00:00")
    LocalDateTime startTime,

    @Schema(description = "출석 종료 시간 (KST 기준 LocalDateTime, 타임존 없이 전송. 예: 2026-03-16T12:00:00)", example = "2026-03-16T12:00:00")
    LocalDateTime endTime,

    @Schema(description = "승인 필요 여부", example = "true")
    boolean requiresApproval,

    @Schema(description = "참여자 Member ID 목록")
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
            startTime.atZone(ScheduleConstants.KST).toInstant(),
            endTime.atZone(ScheduleConstants.KST).toInstant(),
            DEFAULT_LATE_THRESHOLD_MINUTES
        );
        return new CreateAttendanceSheetCommand(scheduleId, gisuId, window, requiresApproval, participantMemberIds);
    }
}
