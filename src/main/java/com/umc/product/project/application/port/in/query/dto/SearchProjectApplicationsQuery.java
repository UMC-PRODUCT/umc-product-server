package com.umc.product.project.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import java.util.Objects;
import lombok.Builder;

/**
 * PM/운영진용 단일 프로젝트 지원자 목록 조회 Query.
 * <p>
 * 임시저장(PENDING) 지원서는 본 API 응답에 노출되지 않으므로, {@link #status} 값으로도 PENDING 은 받지 않는다.
 *
 * @param projectId       대상 프로젝트 ID (path variable)
 * @param matchingRoundId 매칭 차수 필터 (선택). null 이면 전체 차수.
 * @param part            지원 파트 필터 (선택). null 이면 전체 파트.
 * @param status          지원 상태 필터 (선택). null 이면 SUBMITTED/APPROVED/REJECTED 전체. PENDING(임시저장)이 들어오면 도메인 invariant 위반으로
 *                        즉시 예외를 던진다.
 */
@Builder
public record SearchProjectApplicationsQuery(
    Long projectId,
    Long matchingRoundId,
    ChallengerPart part,
    ProjectApplicationStatus status
) {
    public SearchProjectApplicationsQuery {
        Objects.requireNonNull(projectId, "projectId must not be null");
        if (status == ProjectApplicationStatus.DRAFT) {
            throw new ProjectDomainException(
                ProjectErrorCode.APPLICATION_PENDING_FILTER_NOT_ALLOWED);
        }
    }
}
