package com.umc.product.project.application.port.in.command.dto;

import java.util.Objects;
import lombok.Builder;

/**
 * 프로젝트 소유권 양도 Command.
 */
@Builder
public record TransferProjectOwnershipCommand(
    Long projectId,
    Long requesterMemberId,
    Long newOwnerMemberId,
    String reason
) {
    public TransferProjectOwnershipCommand {
        Objects.requireNonNull(projectId, "projectId must not be null");
        Objects.requireNonNull(requesterMemberId, "requesterMemberId must not be null");
        Objects.requireNonNull(newOwnerMemberId, "newOwnerMemberId must not be null");
    }
}
