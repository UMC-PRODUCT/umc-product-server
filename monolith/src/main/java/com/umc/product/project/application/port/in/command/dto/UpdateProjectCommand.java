package com.umc.product.project.application.port.in.command.dto;

import java.util.Objects;
import lombok.Builder;

/**
 * 프로젝트 기본 정보 부분 업데이트 Command (PROJECT-102).
 * <p>
 * {@code projectId}와 {@code requesterMemberId}를 제외한 필드는 모두 nullable이며,
 * {@code null}이면 해당 필드는 변경하지 않습니다.
 * <p>
 * 소유권 변경은 별도 {@link TransferProjectOwnershipCommand} 사용.
 */
@Builder
public record UpdateProjectCommand(
    Long projectId,
    Long requesterMemberId,
    String name,
    String description,
    String externalLink,
    String thumbnailFileId,
    String logoFileId
) {
    public UpdateProjectCommand {
        Objects.requireNonNull(projectId, "projectId must not be null");
        Objects.requireNonNull(requesterMemberId, "requesterMemberId must not be null");
    }
}
