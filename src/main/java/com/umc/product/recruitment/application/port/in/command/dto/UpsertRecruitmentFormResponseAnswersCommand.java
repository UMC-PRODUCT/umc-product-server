package com.umc.product.recruitment.application.port.in.command.dto;

import com.umc.product.survey.domain.enums.QuestionType;
import java.util.List;
import java.util.Map;

public record UpsertRecruitmentFormResponseAnswersCommand(
        Long recruitmentId,
        Long formResponseId,
        List<UpsertItem> items
) {
    public record UpsertItem(
            Long questionId,
            QuestionType answeredAsType,
            Map<String, Object> value
    ) {
    }
}
