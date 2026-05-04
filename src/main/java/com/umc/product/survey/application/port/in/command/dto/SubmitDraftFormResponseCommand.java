package com.umc.product.survey.application.port.in.command.dto;

import lombok.Builder;

/**
 * draft 응답을 SUBMITTED 상태로 전환(최종 제출)하는 Command.
 * <p>
 * 답변 내용은 이전 {@code updateDraft} 로 저장된 값 그대로 유지된다 — 이 Command 는 상태 전이만 담당.
 * 최종 제출 직전에 답변을 한 번 더 바꾸려면 {@code updateDraft} 를 먼저 호출한 뒤 submit.
 * <p>
 * draft 가 아닌 응답을 submit 시도하면 예외. {@code submittedIp} 는 감사/분석용 (선택, null 가능).
 */
@Builder
public record SubmitDraftFormResponseCommand(
    Long formResponseId,
    Long requesterMemberId,
    String submittedIp
) {
}
