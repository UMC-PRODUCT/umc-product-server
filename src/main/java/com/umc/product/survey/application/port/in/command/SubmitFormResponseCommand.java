package com.umc.product.survey.application.port.in.command;

import java.util.List;
import java.util.Map;

public record SubmitFormResponseCommand(
        Long userId,
        Long recruitmentId,
        List<AnswerCommand> answers
) {

    public record AnswerCommand(
            Long questionId,
            Map<String, Object> value
    ) {
    }

}
