package com.umc.product.schedule.adapter.in.web.dto.request;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.schedule.application.port.in.command.dto.CheckAttendanceCommand;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "출석 체크 요청")
public record CheckAttendanceRequest(
    @Schema(description = "출석부 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long attendanceSheetId,

    @Schema(description = "위도", example = "37.5665")
    Double latitude,

    @Schema(description = "경도", example = "126.9780")
    Double longitude,

    @Schema(description = "위치 인증 성공 여부 (프론트에서 판단)", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean locationVerified
) {
    public CheckAttendanceRequest {
        if (attendanceSheetId == null) {
            throw new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.ATTENDANCE_SHEET_NOT_FOUND);
        }
        if (locationVerified == null) {
            throw new IllegalArgumentException("위치 인증 여부는 필수입니다");
        }
    }

    public CheckAttendanceCommand toCommand(Long memberId) {
        return new CheckAttendanceCommand(
            attendanceSheetId,
            memberId,
            Instant.now(),
            latitude,
            longitude,
            locationVerified
        );
    }
}
