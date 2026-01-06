package com.umc.product.curriculum.application.port.in.command;

import com.umc.product.curriculum.domain.enums.WorkbookStatus;

public record ReviewWorkbookCommand(
        Long challengerWorkbookId,
        WorkbookStatus status, // PASS or FAIL
        String feedback // 피드백
) {
}
