package com.umc.product.project.application.port.in.command.dto;

/**
 * PM 이 지원서에 내릴 수 있는 결정.
 * <p>
 * 도메인 enum 을 직접 노출하지 않는 이유:
 * <ul>
 *   <li>DRAFT, CANCELLED 등 결정 대상이 아닌 status 입력을 enum 자체로 차단</li>
 *   <li>도메인 enum 변경이 API 계약 변경을 강제하지 않도록 결합 회피</li>
 * </ul>
 * {@link #SUBMITTED} 는 이미 내린 합격·불합격을 거두고 대기(미판정)로 되돌리는 결정이다. 지원 현황 화면의 "대기" 와 같은 값을 쓴다.
 */
public enum ApplicationDecisionStatus {
    APPROVED,
    REJECTED,
    SUBMITTED
}
