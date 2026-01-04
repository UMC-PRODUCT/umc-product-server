package com.umc.product.form.application.port.in.command;

import java.util.List;

public record SubmitFormResponseCommand(
        Long userId,
        Long formId,
        List<AnswerCommand> answers
) {

    public record AnswerCommand(
            Long questionId,
            Object value
    ) {
    }

}