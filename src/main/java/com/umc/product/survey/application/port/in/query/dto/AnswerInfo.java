package com.umc.product.survey.application.port.in.query.dto;

import com.umc.product.survey.domain.SingleAnswer;
import com.umc.product.survey.domain.enums.QuestionType;
import java.util.Map;

public record AnswerInfo(
        Long questionId,
        Map<String, Object> value,
        QuestionType answeredAsType
) {
    public static AnswerInfo from(SingleAnswer answer) {
        return new AnswerInfo(
                answer.getQuestion().getId(),
                answer.getValue(),
                answer.getAnsweredAsType()
        );
    }
}
