package com.umc.product.organization.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;

/**
 * 학교 기반 접근 컨텍스트
 *
 * <p>인증된 사용자의 역할에 따라 조회 가능한 schoolId와 part를 결정합니다.</p>
 * <ul>
 *   <li>회장/부회장: 본인 학교의 모든 파트 조회 가능 (part = null)</li>
 *   <li>파트장/기타 운영진: 본인 학교의 담당 파트만 조회 가능</li>
 * </ul>
 *
 * @param schoolId 조회 대상 학교 ID
 * @param part     조회 대상 파트 (null이면 모든 파트)
 */
public record SchoolAccessContext(
        Long schoolId,
        ChallengerPart part
) {
}
