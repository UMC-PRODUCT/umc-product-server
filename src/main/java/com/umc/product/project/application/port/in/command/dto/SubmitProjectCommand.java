package com.umc.product.project.application.port.in.command.dto;

import java.util.Objects;
import lombok.Builder;

/**
 * 프로젝트 제출 Command (PROJECT-107).
 * <p>
 * DRAFT 상태의 프로젝트를 PENDING_REVIEW로 상태 전이합니다.
 * 상태 전이 시 필수 필드(name, applicationFormId 등) 완비 검증이 동반됩니다.
 */
@Builder
public record SubmitProjectCommand(
    Long projectId,
    Long requesterMemberId
) {
    public SubmitProjectCommand {
        Objects.requireNonNull(projectId, "projectId must not be null");
        Objects.requireNonNull(requesterMemberId, "requesterMemberId must not be null");
    }
}
