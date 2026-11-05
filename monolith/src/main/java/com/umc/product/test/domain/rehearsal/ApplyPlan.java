package com.umc.product.test.domain.rehearsal;

import java.util.List;
import java.util.Map;

import com.umc.product.common.domain.enums.ChallengerPart;

public record ApplyPlan(List<PlannedApplication> applications, Map<SkipReason, Integer> skipped) {
    public ApplyPlan {
        applications = List.copyOf(applications);
        skipped = Map.copyOf(skipped);
    }

    /**
     * @param draftApplicationId 이 차수에 이어서 제출할 임시저장 지원서. 없으면 새로 만든다
     */
    public record PlannedApplication(Long memberId, String email, Long projectId, ChallengerPart part,
                                     Long draftApplicationId) {
    }
}
