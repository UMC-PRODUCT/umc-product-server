package com.umc.product.survey.application.port.in.command.dto;

import lombok.Builder;

import java.util.List;

/**
 * 한 질문에 대한 응답 입력값.
 *
 * <ul>
 *   <li>SHORT_TEXT / LONG_TEXT: {@code textValue} 사용</li>
 *   <li>RADIO / DROPDOWN: {@code selectedOptionIds} 에 1개</li>
 *   <li>CHECKBOX: {@code selectedOptionIds} 에 1개 이상</li>
 * </ul>
 * <p>
 * SCHEDULE / FILE / PORTFOLIO 타입은 현재 미지원. (Survey UseCase 구현 시 확장 예정)
 */
@Builder
public record AnswerCommand(
    Long questionId,
    String textValue,
    List<Long> selectedOptionIds
) {
}
