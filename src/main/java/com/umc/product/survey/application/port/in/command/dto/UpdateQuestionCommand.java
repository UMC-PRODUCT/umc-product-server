package com.umc.product.survey.application.port.in.command.dto;

import com.umc.product.survey.domain.enums.QuestionType;
import lombok.Builder;

/**
 * 질문 속성 업데이트 Command.
 * type 변경 시 Service가 관련 QuestionOption/AnswerChoice 정리 책임.
 */
@Builder
public record UpdateQuestionCommand(
    Long questionId,
    Long requesterMemberId,
    QuestionType type,
    String title,
    String description,
    boolean isRequired
) {
}
