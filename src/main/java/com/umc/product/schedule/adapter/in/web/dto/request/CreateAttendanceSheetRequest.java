package com.umc.product.schedule.adapter.in.web.dto.request;

import com.umc.product.schedule.application.port.in.command.dto.CreateAttendanceSheetCommand;
import com.umc.product.schedule.domain.exception.ScheduleDomainException;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import com.umc.product.schedule.domain.vo.AttendanceWindow;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "출석부 생성 요청")
public record CreateAttendanceSheetRequest(
    @Schema(description = "기수 ID", example = "1")
    Long gisuId,

    @Schema(description = "출석 시작 시간 (UTC ISO8601. 예: 2026-03-16T01:00:00Z)", example = "2026-03-16T01:00:00Z")
    Instant startTime,

    @Schema(description = "출석 종료 시간 (UTC ISO8601. 예: 2026-03-16T03:00:00Z)", example = "2026-03-16T03:00:00Z")
    Instant endTime,

    @Schema(description = "승인 필요 여부", example = "true")
    boolean requiresApproval,

    @Schema(description = "참여자 Member ID 목록")
    List<Long> participantMemberIds
) {
    private static final int DEFAULT_LATE_THRESHOLD_MINUTES = 10;

    public CreateAttendanceSheetRequest {
        if (gisuId == null) {
            throw new ScheduleDomainException(ScheduleErrorCode.GISU_ID_REQUIRED);
        }

        if (startTime == null || endTime == null) {
            throw new ScheduleDomainException(ScheduleErrorCode.INVALID_TIME_RANGE);
        }

        if (startTime.isAfter(endTime)) {
            throw new ScheduleDomainException(ScheduleErrorCode.INVALID_TIME_RANGE);
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
