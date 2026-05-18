package com.umc.product.project.application.service.policy;

import com.umc.product.project.domain.enums.MatchingType;
import org.springframework.stereotype.Component;

/**
 * PLAN_DEVELOPER 매칭 정책 (지원자가 TO 대비 어느 비율인지로 판정).
 * <ul>
 *   <li>지원자 ≥ TO (100% 이상): 최소 ceil(TO × 0.5) 명</li>
 *   <li>TO 대비 50% 초과 ~ 100% 미만: 최소 ceil(TO × 0.25) 명</li>
 *   <li>50% 이하: 의무 없음</li>
 * </ul>
 */
@Component
public class DeveloperMatchingPolicy implements MatchingDecisionPolicy {

    @Override
    public MatchingType supportedType() {
        return MatchingType.PLAN_DEVELOPER;
    }

    @Override
    public int minimumRequired(int applicantsCount, int quota) {
        if (applicantsCount >= quota) {
            return ceilOfRatio(quota, 0.5);
        }
        if (applicantsCount * 2 > quota) {
            return ceilOfRatio(quota, 0.25);
        }
        return 0;
    }

    private static int ceilOfRatio(int quota, double ratio) {
        return (int) Math.ceil(quota * ratio);
    }
}
