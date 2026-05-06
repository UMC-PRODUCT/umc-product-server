package com.umc.product.project.application.port.in.query.dto;

import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

/**
 * PM/운영진용 지원자 목록 카드의 표시용 status enum.
 * <p>
 * 도메인 ENUM ({@link ProjectApplicationStatus}) 에서 임시저장(PENDING)을 의도적으로 제외한다 -- 본 응답에는 제출 완료된 지원서만 노출되어야 하기 때문. 본인 조회용인
 * {@code MyProjectApplicationCardStatus} 와는 시맨틱이 달라 별도 enum 으로 분리한다.
 * <p>
 * 라벨 매핑:
 * <ul>
 *   <li>SUBMITTED -> "대기" (제출 후 합/불 결정 전)</li>
 *   <li>APPROVED  -> "합격"</li>
 *   <li>REJECTED  -> "불합격"</li>
 * </ul>
 */
public enum ManagedProjectApplicationCardStatus {
    SUBMITTED,
    APPROVED,
    REJECTED;

    /**
     * 도메인 ENUM 으로부터 표시용 status 를 도출한다. PENDING(임시저장)은 본 enum 에 매핑되지 않으므로 호출자(Repository/Service)가 사전 필터링하지 않은 것은 도메인
     * invariant 위반으로 간주하고 예외를 던진다.
     */
    public static ManagedProjectApplicationCardStatus from(ProjectApplicationStatus status) {
        return switch (status) {
            case SUBMITTED -> SUBMITTED;
            case APPROVED -> APPROVED;
            case REJECTED -> REJECTED;
            case PENDING -> throw new ProjectDomainException(
                ProjectErrorCode.APPLICATION_PENDING_NOT_EXPOSABLE);
        };
    }
}
