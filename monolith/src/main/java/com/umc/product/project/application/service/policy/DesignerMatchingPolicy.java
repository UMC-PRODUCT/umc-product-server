package com.umc.product.project.application.service.policy;

import com.umc.product.project.domain.enums.MatchingType;
import org.springframework.stereotype.Component;

/**
 * PLAN_DESIGN 매칭 정책.
 * <ul>
 *   <li>지원자 1명: 선택 의무 없음 (PM 자유)</li>
 *   <li>지원자 2명 이상: 최소 1명 합격 의무</li>
 * </ul>
 * TO 와 무관하다.
 */
@Component
public class DesignerMatchingPolicy implements MatchingDecisionPolicy {

    @Override
    public MatchingType supportedType() {
        return MatchingType.PLAN_DESIGN;
    }

    @Override
    public int minimumRequired(int applicantsCount, int quota) {
        return applicantsCount >= 2 ? 1 : 0;
    }
}
