package com.umc.product.project.application.service.policy;

import org.springframework.stereotype.Component;

import com.umc.product.project.domain.enums.MatchingType;

/**
 * PLAN_DESIGN 매칭 정책.
 * <ul>
 *   <li>잔여 모집 정원 없음: 추가 선발 불가</li>
 *   <li>잔여 모집 정원이 있고 지원자 1명 이하: 선택 의무 없음 (PM 자유)</li>
 *   <li>잔여 모집 정원이 있고 지원자 2명 이상: 최소 1명 합격 의무</li>
 * </ul>
 */
@Component
public class DesignerMatchingPolicy implements MatchingDecisionPolicy {

    @Override
    public MatchingType supportedType() {
        return MatchingType.PLAN_DESIGN;
    }

    @Override
    public int minimumRequired(int applicantsCount, int quota) {
        return quota > 0 && applicantsCount >= 2 ? 1 : 0;
    }
}
