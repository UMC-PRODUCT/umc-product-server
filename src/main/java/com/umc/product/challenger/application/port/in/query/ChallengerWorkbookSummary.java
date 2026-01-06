package com.umc.product.challenger.application.port.in.query;


import com.umc.product.curriculum.domain.enums.WorkbookStatus;

public record ChallengerWorkbookSummary(
        Long challengerWorkbookId,
        Long challengerId,
        String challengerName,
        String schoolName,
        String part,
        String workbookTitle,
        String submission,
        WorkbookStatus status,
        Boolean isBest
) {
}
