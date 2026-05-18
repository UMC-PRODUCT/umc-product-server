package com.umc.product.curriculum.application.port.in.command.dto.workbook;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 베스트 워크북 선정 사유 수정 커맨드 (운영진 전용)
 *
 * @param weeklyBestWorkbookId WeeklyBestWorkbook Entity PK
 * @param requestedMemberId    요청 운영진의 멤버 ID (권한 확인용)
 * @param newReason            변경할 선정 사유
 */
@Builder
public record EditWeeklyBestWorkbookCommand(
    @NotNull(message = "베스트 워크북 ID는 필수입니다")
    Long weeklyBestWorkbookId,

    @NotNull(message = "멤버 ID는 필수입니다")
    Long requestedMemberId,

    @NotBlank(message = "선정 이유는 필수입니다.")
    String newReason
) {
}
