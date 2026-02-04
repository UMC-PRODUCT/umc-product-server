package com.umc.product.curriculum.application.port.in.query.dto;

import com.umc.product.curriculum.domain.ChallengerWorkbook;

public record WorkbookSubmissionDetailInfo(
        Long challengerWorkbookId,
        String submission
) {
    public static WorkbookSubmissionDetailInfo from(ChallengerWorkbook challengerWorkbook) {
        return new WorkbookSubmissionDetailInfo(
                challengerWorkbook.getId(),
                challengerWorkbook.getSubmission()
        );
    }
}
