package com.umc.product.project.application.port.in.command.dto;

import java.util.Objects;
import lombok.Builder;

/**
 * 프로젝트 공개 Command (PROJECT-108).
 * <p>
 * PENDING_REVIEW → IN_PROGRESS 전이 + 지원 폼 동반 publish.
 */
@Builder
public record PublishProjectCommand(
    Long projectId,
    Long requesterMemberId
) {
    public PublishProjectCommand {
        Objects.requireNonNull(projectId, "projectId must not be null");
        Objects.requireNonNull(requesterMemberId, "requesterMemberId must not be null");
    }
}
