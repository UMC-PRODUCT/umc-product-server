package com.umc.product.curriculum.application.port.in.query.dto;

import com.umc.product.curriculum.domain.enums.WorkbookStatus;

public record WorkbookSubmissionInfo(
        Long challengerWorkbookId,
        Long challengerId,
        String memberName,
        String challengerName,
        String profileImageUrl,
        String schoolName,
        String part,
        String workbookTitle,
        WorkbookStatus status
) {
}
