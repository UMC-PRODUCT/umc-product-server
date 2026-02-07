package com.umc.product.schedule.adapter.in.web.dto.request;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.schedule.application.port.in.command.dto.SubmitReasonCommand;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Schema(description = "사유 제출 출석 요청")
public record SubmitReasonRequest(
    @NotNull(message = "출석부 ID는 필수입니다")
    @Schema(description = "출석부 ID", example = "1")
    Long attendanceSheetId,

    @NotBlank(message = "사유는 필수입니다")
    @Schema(description = "출석 불가 사유", example = "개인 사유로 위치 인증이 어렵습니다")
    String reason
) {
    public SubmitReasonRequest {
        if (attendanceSheetId == null) {
            throw new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.ATTENDANCE_SHEET_NOT_FOUND);
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("사유는 필수입니다");
        }
    }

    public SubmitReasonCommand toCommand(Long memberId) {
        return new SubmitReasonCommand(
            attendanceSheetId,
            memberId,
            reason,
            Instant.now()
        );
    }
}
