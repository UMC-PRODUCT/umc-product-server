package com.umc.product.curriculum.application.port.in.query;

import com.umc.product.curriculum.application.port.in.query.dto.WorkbookSubmissionContext;

/**
 * 워크북 제출 현황 조회를 위한 컨텍스트 정보 추출
 * <p>
 * 인증된 사용자의 역할에 따라 조회 가능한 schoolId와 part를 결정합니다.
 * - 회장/부회장: 본인 학교의 모든 파트 조회 가능
 * - 파트장/기타 운영진: 본인 학교의 담당 파트만 조회 가능
 */
public interface GetWorkbookSubmissionContextUseCase {

    /**
     * 사용자의 워크북 제출 현황 조회 컨텍스트를 반환합니다.
     *
     * @param memberId 회원 ID
     * @return 조회 가능한 schoolId와 part 정보
     * @throws IllegalStateException 학교 운영진 역할이 없는 경우 (권한 검증은 @CheckAccess에서 처리되므로 정상적으로는 발생하지 않음)
     */
    WorkbookSubmissionContext getContext(Long memberId);
}
