package com.umc.product.project.application.service.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

class DesignerMatchingPolicyTest {

    private static final long FIXED_SEED = 42L;

    DesignerMatchingPolicy sut = new DesignerMatchingPolicy();

    @Test
    void supportedType은_PLAN_DESIGN을_반환한다() {
        assertThat(sut.supportedType()).isEqualTo(MatchingType.PLAN_DESIGN);
    }

    /**
     * final-api.md §3 매트릭스 ① — TO 무관, 지원자 수 자체로 판정.
     * <pre>
     * 지원자 1명     → 0 (자유)
     * 지원자 2명 이상 → 1
     * </pre>
     */
    @Nested
    class minimumRequired {

        @ParameterizedTest(name = "지원자 {0}명 (TO {1}) → 최소 {2}명")
        @CsvSource({
            "1, 1, 0",
            "1, 2, 0",
            "1, 5, 0",
            "2, 1, 1",
            "2, 5, 1",
            "3, 2, 1",
            "5, 1, 1",
            "10, 3, 1"
        })
        void TO_무관_지원자_수만으로_결정(int applicants, int quota, int expectedMin) {
            assertThat(sut.minimumRequired(applicants, quota)).isEqualTo(expectedMin);
        }
    }

    @Nested
    class decideAutomatically {

        @Test
        void 지원자_1명만_있고_의무_없음이면_SUBMITTED는_REJECTED로_확정() {
            List<ProjectApplication> applicants = List.of(
                application(1L, ProjectApplicationStatus.SUBMITTED)
            );

            AutoDecisionResult result = sut.decideAutomatically(applicants, 1, new Random(FIXED_SEED));

            assertThat(result.approvedIds()).isEmpty();
            assertThat(result.rejectedIds()).containsExactly(1L);
        }

        @Test
        void PM이_이미_충분히_APPROVED했으면_SUBMITTED는_REJECTED로_확정() {
            List<ProjectApplication> applicants = List.of(
                application(1L, ProjectApplicationStatus.APPROVED),
                application(2L, ProjectApplicationStatus.SUBMITTED),
                application(3L, ProjectApplicationStatus.SUBMITTED)
            );

            AutoDecisionResult result = sut.decideAutomatically(applicants, 1, new Random(FIXED_SEED));

            assertThat(result.approvedIds()).containsExactly(1L);
            assertThat(result.rejectedIds()).containsExactlyInAnyOrder(2L, 3L);
        }

        @Test
        void 지원자_2명이상이고_PM이_0명_APPROVED면_SUBMITTED중_random_1명_합격() {
            List<ProjectApplication> applicants = List.of(
                application(1L, ProjectApplicationStatus.SUBMITTED),
                application(2L, ProjectApplicationStatus.SUBMITTED),
                application(3L, ProjectApplicationStatus.SUBMITTED)
            );

            AutoDecisionResult result = sut.decideAutomatically(applicants, 1, new Random(FIXED_SEED));

            assertThat(result.approvedIds()).hasSize(1);
            assertThat(result.rejectedIds()).hasSize(2);
            assertThat(result.approvedIds()).isSubsetOf(1L, 2L, 3L);
        }

        @Test
        void PM이_모두_REJECTED했고_SUBMITTED_없으면_REJECTED_중_random_1명_합격_override() {
            List<ProjectApplication> applicants = List.of(
                application(1L, ProjectApplicationStatus.REJECTED),
                application(2L, ProjectApplicationStatus.REJECTED),
                application(3L, ProjectApplicationStatus.REJECTED)
            );

            AutoDecisionResult result = sut.decideAutomatically(applicants, 1, new Random(FIXED_SEED));

            assertThat(result.approvedIds()).hasSize(1);
            assertThat(result.rejectedIds()).hasSize(2);
            assertThat(result.approvedIds()).isSubsetOf(1L, 2L, 3L);
        }

        @Test
        void SUBMITTED와_REJECTED가_섞여있고_의무_미충족이면_SUBMITTED_우선_random_보충() {
            List<ProjectApplication> applicants = List.of(
                application(1L, ProjectApplicationStatus.REJECTED),
                application(2L, ProjectApplicationStatus.SUBMITTED),
                application(3L, ProjectApplicationStatus.REJECTED)
            );

            AutoDecisionResult result = sut.decideAutomatically(applicants, 1, new Random(FIXED_SEED));

            assertThat(result.approvedIds()).containsExactly(2L);
            assertThat(result.rejectedIds()).containsExactlyInAnyOrder(1L, 3L);
        }

        @Test
        void 동일_seed면_동일_결과를_반환한다() {
            List<ProjectApplication> applicants = List.of(
                application(1L, ProjectApplicationStatus.SUBMITTED),
                application(2L, ProjectApplicationStatus.SUBMITTED),
                application(3L, ProjectApplicationStatus.SUBMITTED)
            );

            AutoDecisionResult first = sut.decideAutomatically(applicants, 1, new Random(FIXED_SEED));
            AutoDecisionResult second = sut.decideAutomatically(applicants, 1, new Random(FIXED_SEED));

            assertThat(first).isEqualTo(second);
        }

        @Test
        void approvedIds와_rejectedIds는_disjoint하다() {
            List<ProjectApplication> applicants = List.of(
                application(1L, ProjectApplicationStatus.APPROVED),
                application(2L, ProjectApplicationStatus.SUBMITTED),
                application(3L, ProjectApplicationStatus.REJECTED)
            );

            AutoDecisionResult result = sut.decideAutomatically(applicants, 1, new Random(FIXED_SEED));

            assertThat(result.approvedIds()).doesNotContainAnyElementsOf(result.rejectedIds());
        }
    }

    private ProjectApplication application(Long id, ProjectApplicationStatus status) {
        try {
            var constructor = ProjectApplication.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            ProjectApplication application = constructor.newInstance();
            ReflectionTestUtils.setField(application, "id", id);
            ReflectionTestUtils.setField(application, "status", status);
            return application;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
