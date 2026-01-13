package com.umc.product.survey.application.port.in.command.dto;

import java.util.List;
import java.util.Map;

public record UpdateDraftFormResponseCommand(
        Long memberId,
        Long formId,
        List<AnswerCommand> answers
) {

    public record AnswerCommand(
            Long questionId,
            Map<String, Object> value
    ) {
    }

}
