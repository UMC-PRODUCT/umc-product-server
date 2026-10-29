package com.umc.product.project.application.port.in.command.dto;

import java.util.Objects;

import lombok.Builder;

/**
 * 프로젝트 제출 Command (PROJECT-107).
 * <p>
 * DRAFT 상태의 프로젝트를 제출합니다. 운영진이 제출하면 검증 후 즉시 공개합니다.
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
