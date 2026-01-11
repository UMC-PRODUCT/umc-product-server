package com.umc.product.survey.application.port.in.query;

import java.util.Map;

public record AnswerInfo(
        Long questionId,
        Map<String, Object> value
) {
}
