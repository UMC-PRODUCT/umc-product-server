package com.umc.product.project.application.port.in.command.dto;

import java.util.Objects;
import lombok.Builder;

/**
 * 프로젝트 멤버 제거 Command (PROJECT-005).
 * <p>
 * 정책:
 * <ul>
 *   <li>메인 PM 제거 거부 — 소유권 양도 API({@code PROJECT-104}) 로 유도</li>
 *   <li>DRAFT/PENDING_REVIEW: hard delete (실수 정정)</li>
 *   <li>IN_PROGRESS: soft delete (status = DISMISSED, 매칭/출석 등 외부 도메인 무결성)</li>
 *   <li>COMPLETED/ABORTED: 거부 (도메인 가드)</li>
 * </ul>
 */
@Builder
public record RemoveProjectMemberCommand(
    Long projectId,
    Long memberId,
    String reason,
    Long requesterMemberId
) {
    public RemoveProjectMemberCommand {
        Objects.requireNonNull(projectId, "projectId must not be null");
        Objects.requireNonNull(memberId, "memberId must not be null");
        Objects.requireNonNull(requesterMemberId, "requesterMemberId must not be null");
    }
}
