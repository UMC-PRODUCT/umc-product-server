package com.umc.product.survey.application.port.in.command;

import java.util.List;
import java.util.Map;

public record UpdateDraftFormResponseCommand(
        Long userId,
        Long formId,
        List<AnswerCommand> answers
) {

    public record AnswerCommand(
            Long questionId,
            Map<String, Object> value
    ) {
    }

}
