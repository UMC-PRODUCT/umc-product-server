package com.umc.product.project.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.project.application.service.policy.DesignerMatchingPolicy;
import com.umc.product.project.application.service.policy.DeveloperMatchingPolicy;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.exception.ProjectDomainException;

@DisplayName("최소 선발 인원 조회")
class MatchingSelectionRuleQueryServiceTest {

    private final MatchingSelectionRuleQueryService service = new MatchingSelectionRuleQueryService(
        List.of(new DesignerMatchingPolicy(), new DeveloperMatchingPolicy()));

    @Test
    @DisplayName("매칭 유형마다 자동 선발 정책과 같은 최소 인원을 돌려준다")
    void delegatesToPolicyOfType() {
        assertThat(service.minimumRequired(MatchingType.PLAN_DEVELOPER, 9, 6)).isEqualTo(3);
        assertThat(service.minimumRequired(MatchingType.PLAN_DEVELOPER, 4, 6)).isEqualTo(2);
        assertThat(service.minimumRequired(MatchingType.PLAN_DEVELOPER, 3, 6)).isZero();
        assertThat(service.minimumRequired(MatchingType.PLAN_DESIGN, 2, 1)).isEqualTo(1);
        assertThat(service.minimumRequired(MatchingType.PLAN_DESIGN, 1, 1)).isZero();
    }

    @Test
    @DisplayName("정책이 없는 유형이면 PROJECT_MATCHING_ROUND_POLICY_NOT_FOUND를 던진다")
    void missingPolicyThrows() {
        MatchingSelectionRuleQueryService empty = new MatchingSelectionRuleQueryService(List.of());

        assertThatThrownBy(() -> empty.minimumRequired(MatchingType.PLAN_DESIGN, 2, 1))
            .isInstanceOf(ProjectDomainException.class);
    }
}
