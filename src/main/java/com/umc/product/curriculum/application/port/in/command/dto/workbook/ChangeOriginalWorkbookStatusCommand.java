package com.umc.product.curriculum.application.port.in.command.dto.workbook;

import com.umc.product.curriculum.domain.enums.OriginalWorkbookStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 원본 워크북 상태 변경 커맨드
 *
 * @param originalWorkbookId 상태를 변경할 원본 워크북 ID
 * @param status             변경할 상태 (READY 또는 RELEASED)
 * @param operatorMemberId   요청 운영진의 멤버 ID (RELEASED 전환 시 releasedMemberId로 기록)
 */
@Builder
public record ChangeOriginalWorkbookStatusCommand(
    @NotNull(message = "원본 워크북 ID는 필수입니다")
    Long originalWorkbookId,

    @NotNull(message = "변경할 상태는 필수입니다")
    OriginalWorkbookStatus status,

    @NotNull(message = "요청자 멤버 ID는 필수입니다")
    Long operatorMemberId
) {
}
