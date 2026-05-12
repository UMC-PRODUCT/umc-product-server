package com.umc.product.project.application.port.in.query.dto;

import com.umc.product.project.domain.enums.ProjectApplicationStatus;

/**
 * 지원서 상태를 화면에 노출하기 위한 표시용 enum.
 * <p>
 * 도메인 enum ({@link ProjectApplicationStatus}) 과 1:1 로 매핑되지만, 화면 응답 전용으로 분리되어 있어 향후 표시용 상태(예: 매칭 결과 라벨)가 추가되어도 도메인 enum
 * 의 시맨틱을 흔들지 않는다.
 * <p>
 * 본 enum 은 임시저장(DRAFT)을 그대로 포함한다. 따라서 DRAFT 노출이 허용되는 호출 시점에서 사용하며, 현재 사용처는 다음과 같다.
 * <ul>
 *   <li>본인 지원 내역 카드 — 지원자가 자신의 임시저장본을 다시 열어볼 수 있어야 함</li>
 *   <li>지원서 단건 상세 조회 API — PM/운영진/지부장/CC/지원자 본인 모두 동일 응답을 받으며,
 *       지원자 본인 호출 시 임시저장 상태도 그대로 노출되어야 함 (권한 분기는 별도 처리)</li>
 * </ul>
 * <p>
 * DRAFT 를 절대 노출해서는 안 되는 PM/운영진 지원자 목록 응답에는 본 enum 대신
 * {@link ManagedProjectApplicationCardStatus} 를 사용한다 — 두 enum 은 DRAFT 포함 여부에서 의도적으로 다르므로 혼용하지 않는다.
 */
public enum ProjectApplicationViewStatus {
    DRAFT,
    SUBMITTED,
    APPROVED,
    REJECTED,
    CANCELLED;

    public static ProjectApplicationViewStatus from(ProjectApplicationStatus status) {
        return switch (status) {
            case DRAFT -> DRAFT;
            case SUBMITTED -> SUBMITTED;
            case APPROVED -> APPROVED;
            case REJECTED -> REJECTED;
            case CANCELLED -> CANCELLED;
        };
    }
}
