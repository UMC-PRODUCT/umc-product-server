package com.umc.product.organization.application.port.in.query;

import com.umc.product.organization.application.port.in.query.dto.SchoolAccessContext;

/**
 * 학교 기반 접근 컨텍스트 추출 UseCase
 *
 * <p>인증된 사용자의 역할에 따라 조회 가능한 schoolId와 part를 결정합니다.</p>
 * <ul>
 *   <li>회장/부회장: 본인 학교의 모든 파트 조회 가능</li>
 *   <li>파트장/기타 운영진: 본인 학교의 담당 파트만 조회 가능</li>
 * </ul>
 */
public interface GetSchoolAccessContextUseCase {

    /**
     * 사용자의 학교 접근 컨텍스트를 반환합니다.
     *
     * @param memberId 회원 ID
     * @return 조회 가능한 schoolId와 part 정보
     * @throws IllegalStateException 학교 운영진 역할이 없는 경우
     */
    SchoolAccessContext getContext(Long memberId);
}
