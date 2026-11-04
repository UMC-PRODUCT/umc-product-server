package com.umc.product.project.application.service.query;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.umc.product.project.application.port.in.query.GetMatchingSelectionRuleUseCase;
import com.umc.product.project.application.service.policy.MatchingDecisionPolicy;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

@Service
public class MatchingSelectionRuleQueryService implements GetMatchingSelectionRuleUseCase {

    private final Map<MatchingType, MatchingDecisionPolicy> policies = new EnumMap<>(MatchingType.class);

    public MatchingSelectionRuleQueryService(List<MatchingDecisionPolicy> policies) {
        policies.forEach(policy -> this.policies.put(policy.supportedType(), policy));
    }

    @Override
    public int minimumRequired(MatchingType type, int applicantsCount, int remainingQuota) {
        MatchingDecisionPolicy policy = policies.get(type);
        if (policy == null) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_POLICY_NOT_FOUND);
        }
        return policy.minimumRequired(applicantsCount, remainingQuota);
    }
}
