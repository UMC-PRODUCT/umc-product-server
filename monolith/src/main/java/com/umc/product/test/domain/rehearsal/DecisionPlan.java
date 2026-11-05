package com.umc.product.test.domain.rehearsal;

import java.util.List;
import java.util.Map;

/**
 * 실행하는 쪽은 합격을 모두 끝낸 뒤 불합격을 한다.
 */
public record DecisionPlan(List<PlannedDecision> approvals, List<PlannedDecision> rejections,
                           Map<SkipReason, Integer> skipped) {
    public DecisionPlan {
        approvals = List.copyOf(approvals);
        rejections = List.copyOf(rejections);
        skipped = Map.copyOf(skipped);
    }

    /**
     * @param decidedBy 프로젝트 PO
     */
    public record PlannedDecision(Long projectId, Long applicationId, Long decidedBy) {
    }
}
