package com.umc.product.schedule.adapter.in.web.dto.request;

import com.umc.product.schedule.application.port.in.command.dto.SubmitReasonCommand;
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
    public SubmitReasonCommand toCommand(Long memberId) {
        return new SubmitReasonCommand(
            attendanceSheetId,
            memberId,
            reason,
            Instant.now()
        );
    }
}
