package com.umc.product.curriculum.application.port.in.query.dto;

import com.umc.product.curriculum.domain.ChallengerWorkbook;
import com.umc.product.global.exception.NotImplementedException;

public record WorkbookSubmissionDetailInfo(
    Long challengerWorkbookId,
    String submission
) {
    public static WorkbookSubmissionDetailInfo from(ChallengerWorkbook challengerWorkbook) {
        throw new NotImplementedException();
//        return new WorkbookSubmissionDetailInfo(
//                challengerWorkbook.getId(),
//                challengerWorkbook.getSubmission()
//        );
    }
}
