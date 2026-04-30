package com.umc.product.survey.application.port.in.command.dto;

import lombok.Builder;

/**
 * 선택지 속성 업데이트 Command. content 및 isOther 를 수정.
 * 순서 변경은 {@code ReorderQuestionOptionsCommand}.
 */
@Builder
public record UpdateQuestionOptionCommand(
    Long optionId,
    Long requesterMemberId,
    String content,
    boolean isOther
) {
}
