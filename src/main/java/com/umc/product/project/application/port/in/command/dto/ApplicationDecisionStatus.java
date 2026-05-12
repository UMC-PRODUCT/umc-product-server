package com.umc.product.project.application.port.in.command.dto;

/**
 * PM 이 지원서에 내릴 수 있는 결정.
 * <p>
 * UI 의 "대기" 옵션은 {@code PENDING} 으로 표현하며, 도메인 레이어에서
 * {@link com.umc.product.project.domain.enums.ProjectApplicationStatus#SUBMITTED} 로 매핑된다.
 * <p>
 * 도메인 enum 을 직접 노출하지 않는 이유:
 * <ul>
 *   <li>DRAFT 등 결정 대상이 아닌 status 입력을 enum 자체로 차단</li>
 *   <li>도메인 enum 변경이 API 계약 변경을 강제하지 않도록 결합 회피</li>
 * </ul>
 */
public enum ApplicationDecisionStatus {
    APPROVED,
    REJECTED,
    PENDING
}
