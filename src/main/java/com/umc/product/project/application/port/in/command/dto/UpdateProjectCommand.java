package com.umc.product.project.application.port.in.command.dto;

import java.util.Objects;
import lombok.Builder;

/**
 * 프로젝트 기본 정보 부분 업데이트 Command (PROJECT-102).
 * <p>
 * {@code projectId}와 {@code requesterMemberId}를 제외한 필드는 모두 nullable이며,
 * {@code null}이면 해당 필드는 <b>수정하지 않음</b>(부분 업데이트 의미)입니다.
 * <p>
 * 보조 PM 목록은 이 Command로 관리하지 않습니다. 별도 팀원 관리 API로 처리됩니다.
 */
@Builder
public record UpdateProjectCommand(
    Long projectId,
    Long requesterMemberId,
    String name,
    String description,
    String externalLink,
    String thumbnailFileId,
    String logoFileId,
    Long productOwnerMemberId
) {
    public UpdateProjectCommand {
        Objects.requireNonNull(projectId, "projectId must not be null");
        Objects.requireNonNull(requesterMemberId, "requesterMemberId must not be null");
    }
}
