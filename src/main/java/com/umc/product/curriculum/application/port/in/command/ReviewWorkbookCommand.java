package com.umc.product.curriculum.application.port.in.command;

import com.umc.product.curriculum.domain.enums.WorkbookStatus;
import jakarta.validation.constraints.NotNull;

/**
 * 워크북 검토(승인/반려) 커맨드 + * @param challengerWorkbookId 검토 대상 워크북 ID
 *
 * @param status   검토 결과 (PASS: 승인, FAIL: 반려)
 * @param feedback 피드백
 */
public record ReviewWorkbookCommand(
        @NotNull
        Long challengerWorkbookId,
        @NotNull
        WorkbookStatus status,
        String feedback
) {
}
