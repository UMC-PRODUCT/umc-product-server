package com.umc.product.project.application.port.in.command.dto;

import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import java.util.Objects;
import lombok.Builder;

/**
 * 프로젝트 중단(abort) Command.
 * <p>
 * IN_PROGRESS 상태 프로젝트를 ABORTED 로 전이하고, ACTIVE 멤버는 WITHDRAWN, 진행 중 application 은 CANCELLED 로 동기화합니다.
 * 종료 상태(COMPLETED/ABORTED)는 도메인 가드({@code Project#abort})가 거부합니다.
 */
@Builder
public record AbortProjectCommand(
    Long projectId,
    Long requesterMemberId,
    String reason
) {
    public AbortProjectCommand {
        Objects.requireNonNull(projectId, "projectId must not be null");
        Objects.requireNonNull(requesterMemberId, "requesterMemberId must not be null");
        if (reason == null || reason.isBlank()) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_ABORT_REASON_REQUIRED);
        }
    }
}
