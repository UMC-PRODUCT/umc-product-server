package com.umc.product.test.domain.rehearsal;

import java.util.List;
import java.util.Map;

import com.umc.product.project.domain.enums.ProjectApplicationStatus;

/**
 * @param decisions 합격을 모두 앞에, 불합격을 뒤에 둔다
 */
public record DecisionPlan(List<PlannedDecision> decisions, Map<SkipReason, Integer> skipped) {
    public DecisionPlan {
        decisions = List.copyOf(decisions);
        skipped = Map.copyOf(skipped);
    }

    /**
     * @param decidedBy 프로젝트 PO
     * @param target    APPROVED 또는 REJECTED
     */
    public record PlannedDecision(Long projectId, Long applicationId, Long decidedBy, ProjectApplicationStatus target) {
    }
}
