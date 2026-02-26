package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.command.dto.UpsertRecruitmentFormResponseAnswersInfo;
import java.util.List;

public record UpsertRecruitmentFormResponseAnswersResponse(
    Long formResponseId,
    List<Long> savedQuestionIds
) {
    public static UpsertRecruitmentFormResponseAnswersResponse from(UpsertRecruitmentFormResponseAnswersInfo result) {
        return new UpsertRecruitmentFormResponseAnswersResponse(
            result.formResponseId(),
            result.savedQuestionIds()
        );
    }
}
