package com.umc.product.survey.application.port.out.dto;

import com.umc.product.survey.domain.enums.QuestionType;

import java.util.Map;

public record VoteAnswerRow(
    Long respondentMemberId,
    QuestionType answeredAsType,
    Map<String, Object> value
) {}
