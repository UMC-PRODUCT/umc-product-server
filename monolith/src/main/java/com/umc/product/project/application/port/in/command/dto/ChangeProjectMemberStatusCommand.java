package com.umc.product.project.application.port.in.command.dto;

import java.util.Objects;

import com.umc.product.project.domain.enums.ProjectMemberStatus;

import lombok.Builder;

/**
 * 프로젝트 팀원 활동 완료·중도 이탈·퇴출 Command (PROJECT-006).
 * <p>
 * 행을 보존한 채 {@code status} 로 변경하고 {@code reason} 을 최근 상태 변경 사유로 기록한다.
 * <p>
 * 정책:
 * <ul>
 *   <li>메인 PM 상태 변경 거부 — 소유권 양도 API({@code PROJECT-104}) 로 유도</li>
 *   <li>COMPLETED/ABORTED 프로젝트: 거부 — 종료된 프로젝트의 이력 고정</li>
 * </ul>
 */
@Builder
public record ChangeProjectMemberStatusCommand(
    Long projectId,
    Long memberId,
    ProjectMemberStatus status,
    String reason,
    Long requesterMemberId
) {
    public ChangeProjectMemberStatusCommand {
        Objects.requireNonNull(projectId, "projectId must not be null");
        Objects.requireNonNull(memberId, "memberId must not be null");
        Objects.requireNonNull(status, "status must not be null");
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be null or blank");
        }
        Objects.requireNonNull(requesterMemberId, "requesterMemberId must not be null");
    }
}
