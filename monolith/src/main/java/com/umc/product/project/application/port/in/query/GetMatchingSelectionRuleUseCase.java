package com.umc.product.project.application.port.in.query;

import com.umc.product.project.domain.enums.MatchingType;

/**
 * 자동 선발이 지키는 최소 선발 인원 조회. PM 결정 자체에는 이 검사가 없고, 자동 선발이 모자란 인원을 채운다.
 */
public interface GetMatchingSelectionRuleUseCase {

    /**
     * @param applicantsCount 그 차수 그 프로젝트·파트 지원자 수(취소 제외, 상태 무관)
     */
    int minimumRequired(MatchingType type, int applicantsCount, int remainingQuota);
}
