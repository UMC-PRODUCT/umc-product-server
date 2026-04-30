package com.umc.product.survey.application.port.in.command.dto;

import lombok.Builder;

/**
 * DRAFT 상태 폼의 메타데이터 업데이트 Command. (임시저장 용도)
 * {@code requesterMemberId}는 권한 검증용 — 폼 작성자 본인인지 확인.
 */
@Builder
public record UpdateFormCommand(
    Long formId,
    Long requesterMemberId,
    String title,
    String description,
    boolean isAnonymous
) {
}
