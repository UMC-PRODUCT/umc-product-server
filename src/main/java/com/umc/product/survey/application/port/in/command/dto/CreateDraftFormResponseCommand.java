package com.umc.product.survey.application.port.in.command.dto;

import lombok.Builder;

/**
 * 폼에 대한 draft 응답을 최초 생성하는 Command.
 * <p>
 * 빈 draft 를 생성하고 ID를 반환한다. 이후 {@code updateDraft} 로 답변을 추가하며 {@code submitDraft} 로 최종 제출한다.
 * <p>
 * 같은 폼에 {@code respondentMemberId} 의 draft 가 이미 있으면 예외.
 */
@Builder
public record CreateDraftFormResponseCommand(
    Long formId,
    Long respondentMemberId
) {
}
