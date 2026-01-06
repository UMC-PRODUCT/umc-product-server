package com.umc.product.curriculum.application.port.in.dto;

import com.umc.product.curriculum.domain.WorkbookStatus;

public record ReviewWorkbookCommand(
        Long challengerWorkbookId,
        WorkbookStatus status, // PASS or FAIL
        String feedback // 피드백
) {
}
