package com.umc.product.recruitment.application.port.in.command.dto;

import java.util.List;

public record UpsertRecruitmentFormResponseAnswersInfo(
        Long formResponseId,
        List<Long> savedQuestionIds
) {
    public static UpsertRecruitmentFormResponseAnswersInfo of(Long formResponseId, List<Long> questionIds) {
        return new UpsertRecruitmentFormResponseAnswersInfo(formResponseId, questionIds);
    }
}
