package com.umc.product.project.application.service.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

class DeveloperMatchingPolicyTest {

    private static final long FIXED_SEED = 42L;

    DeveloperMatchingPolicy sut = new DeveloperMatchingPolicy();

    @Test
    void supportedType은_PLAN_DEVELOPER를_반환한다() {
        assertThat(sut.supportedType()).isEqualTo(MatchingType.PLAN_DEVELOPER);
    }

    /**
     * final-api.md §3 매트릭스 ② — 지원자 비율로 분기.
     * <pre>
     * 지원자 ≥ TO (100%↑)        → ceil(TO × 0.5)
     * 50% 초과 ~ 100% 미만        → ceil(TO × 0.25)
     * 50% 이하                   → 0
     * </pre>
     */
    @Nested
    class minimumRequired {

        @ParameterizedTest(name = "TO {0}, 지원자 100% 이상({1}명) → 최소 {2}명 (50% rule)")
        @CsvSource({
            "3, 3, 2",
            "3, 5, 2",
            "4, 4, 2",
            "4, 10, 2",
            "5, 5, 3",
            "6, 6, 3",
            "7, 7, 4",
            "8, 8, 4"
        })
        void TO_대비_100퍼센트_이상이면_50퍼센트_올림(int quota, int applicants, int expectedMin) {
            assertThat(sut.minimumRequired(applicants, quota)).isEqualTo(expectedMin);
        }

        @ParameterizedTest(name = "TO {0}, 지원자 50%~100%({1}명) → 최소 {2}명 (25% rule)")
        @CsvSource({
            "3, 2, 1",
            "4, 3, 1",
            "5, 3, 2",
            "5, 4, 2",
            "6, 4, 2",
            "6, 5, 2",
            "7, 5, 2",
            "7, 6, 2",
            "8, 5, 2",
            "8, 7, 2"
        })
        void TO_대비_50퍼센트_초과_100퍼센트_미만이면_25퍼센트_올림(int quota, int applicants, int expectedMin) {
            assertThat(sut.minimumRequired(applicants, quota)).isEqualTo(expectedMin);
        }

        @ParameterizedTest(name = "TO {0}, 지원자 50% 이하({1}명) → 의무 없음")
        @CsvSource({
            "3, 1, 0",
            "4, 1, 0",
            "4, 2, 0",
            "5, 2, 0",
            "6, 2, 0",
            "6, 3, 0",
            "7, 3, 0",
            "8, 3, 0",
            "8, 4, 0"
        })
        void TO_대비_50퍼센트_이하면_의무_없음(int quota, int applicants, int expectedMin) {
            assertThat(sut.minimumRequired(applicants, quota)).isEqualTo(expectedMin);
        }
    }

    @Nested
    class decideAutomatically {

        @Test
        void 의무_없음이면_SUBMITTED는_REJECTED로_확정() {
            List<ProjectApplication> applicants = applications(2, ProjectApplicationStatus.SUBMITTED);

            AutoDecisionResult result = sut.decideAutomatically(applicants, 5, new Random(FIXED_SEED));

            assertThat(result.approvedIds()).isEmpty();
            assertThat(result.rejectedIds()).hasSize(2);
        }

        @Test
        void 의무_충족이면_SUBMITTED는_REJECTED로_확정() {
            List<ProjectApplication> applicants = new ArrayList<>();
            applicants.addAll(applicationsWithIds(List.of(1L, 2L, 3L), ProjectApplicationStatus.APPROVED));
            applicants.addAll(applicationsWithIds(List.of(4L, 5L, 6L), ProjectApplicationStatus.SUBMITTED));

            AutoDecisionResult result = sut.decideAutomatically(applicants, 6, new Random(FIXED_SEED));

            assertThat(result.approvedIds()).containsExactlyInAnyOrder(1L, 2L, 3L);
            assertThat(result.rejectedIds()).containsExactlyInAnyOrder(4L, 5L, 6L);
        }

        @Test
        void 의무_미충족이면_SUBMITTED_우선_random_보충() {
            List<ProjectApplication> applicants = new ArrayList<>();
            applicants.addAll(applicationsWithIds(List.of(1L), ProjectApplicationStatus.APPROVED));
            applicants.addAll(applicationsWithIds(List.of(2L, 3L, 4L, 5L, 6L), ProjectApplicationStatus.SUBMITTED));

            AutoDecisionResult result = sut.decideAutomatically(applicants, 6, new Random(FIXED_SEED));

            assertThat(result.approvedIds()).hasSize(3);
            assertThat(result.approvedIds()).contains(1L);
            assertThat(result.approvedIds()).isSubsetOf(1L, 2L, 3L, 4L, 5L, 6L);
            assertThat(result.rejectedIds()).hasSize(3);
        }

        @Test
        void SUBMITTED_부족하면_REJECTED에서_추가_보충_PM_override() {
            List<ProjectApplication> applicants = new ArrayList<>();
            applicants.addAll(applicationsWithIds(List.of(1L), ProjectApplicationStatus.APPROVED));
            applicants.addAll(applicationsWithIds(List.of(2L), ProjectApplicationStatus.SUBMITTED));
            applicants.addAll(applicationsWithIds(List.of(3L, 4L, 5L, 6L), ProjectApplicationStatus.REJECTED));

            AutoDecisionResult result = sut.decideAutomatically(applicants, 6, new Random(FIXED_SEED));

            assertThat(result.approvedIds()).hasSize(3);
            assertThat(result.approvedIds()).contains(1L, 2L);
            assertThat(result.rejectedIds()).hasSize(3);
        }

        @Test
        void PM이_모두_REJECTED했고_SUBMITTED_없으면_REJECTED_중_random_보충() {
            List<ProjectApplication> applicants = applicationsWithIds(
                List.of(1L, 2L, 3L, 4L, 5L, 6L), ProjectApplicationStatus.REJECTED
            );

            AutoDecisionResult result = sut.decideAutomatically(applicants, 6, new Random(FIXED_SEED));

            assertThat(result.approvedIds()).hasSize(3);
            assertThat(result.rejectedIds()).hasSize(3);
            assertThat(result.approvedIds()).isSubsetOf(1L, 2L, 3L, 4L, 5L, 6L);
        }

        @Test
        void TO_50퍼센트_이하라_의무_없는_상황에서_SUBMITTED는_REJECTED로_확정() {
            List<ProjectApplication> applicants = applications(2, ProjectApplicationStatus.SUBMITTED);

            AutoDecisionResult result = sut.decideAutomatically(applicants, 6, new Random(FIXED_SEED));

            assertThat(result.approvedIds()).isEmpty();
            assertThat(result.rejectedIds()).hasSize(2);
        }

        @Test
        void approvedIds와_rejectedIds는_disjoint하다() {
            List<ProjectApplication> applicants = new ArrayList<>();
            applicants.addAll(applicationsWithIds(List.of(1L), ProjectApplicationStatus.APPROVED));
            applicants.addAll(applicationsWithIds(List.of(2L, 3L), ProjectApplicationStatus.SUBMITTED));
            applicants.addAll(applicationsWithIds(List.of(4L, 5L), ProjectApplicationStatus.REJECTED));

            AutoDecisionResult result = sut.decideAutomatically(applicants, 5, new Random(FIXED_SEED));

            assertThat(result.approvedIds()).doesNotContainAnyElementsOf(result.rejectedIds());
        }
    }

    private List<ProjectApplication> applications(int count, ProjectApplicationStatus status) {
        List<ProjectApplication> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(application((long) i, status));
        }
        return list;
    }

    private List<ProjectApplication> applicationsWithIds(List<Long> ids, ProjectApplicationStatus status) {
        return ids.stream().map(id -> application(id, status)).toList();
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
