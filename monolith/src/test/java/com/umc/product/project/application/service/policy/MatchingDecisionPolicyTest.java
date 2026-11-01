package com.umc.product.project.application.service.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;

class MatchingDecisionPolicyTest {

    private static final Long PROJECT_ID = 100L;

    @ParameterizedTest
    @EnumSource(MatchingType.class)
    void 디자인과_개발자_모두_같은_학교_인원이_적은_지원자를_우선한다(MatchingType type) {
        // given
        MatchingSchoolContext schoolContext = new MatchingSchoolContext(Map.of(10L, 1L, 1L, 1L, 2L, 2L));
        schoolContext.addMember(PROJECT_ID, 10L);
        List<ProjectApplication> applicants = List.of(
            application(1L, ProjectApplicationStatus.SUBMITTED), application(2L, ProjectApplicationStatus.SUBMITTED));

        // when
        AutoDecisionResult result = policy(type).decideAutomatically(applicants, 2, new Random(42), schoolContext);

        // then
        assertThat(result.approvedIds()).containsExactly(2L);
        assertThat(result.rejectedIds()).containsExactly(1L);
    }

    @ParameterizedTest
    @EnumSource(MatchingType.class)
    void 학교가_중복되어도_SUBMITTED를_REJECTED보다_먼저_선발한다(MatchingType type) {
        // given
        MatchingSchoolContext schoolContext = new MatchingSchoolContext(Map.of(10L, 1L, 1L, 1L, 2L, 2L));
        schoolContext.addMember(PROJECT_ID, 10L);
        List<ProjectApplication> applicants = List.of(
            application(1L, ProjectApplicationStatus.SUBMITTED), application(2L, ProjectApplicationStatus.REJECTED));

        // when
        AutoDecisionResult result = policy(type).decideAutomatically(applicants, 2, new Random(42), schoolContext);

        // then
        assertThat(result.approvedIds()).containsExactly(1L);
        assertThat(result.rejectedIds()).containsExactly(2L);
    }

    @ParameterizedTest
    @EnumSource(MatchingType.class)
    void REJECTED에서_보충할_때도_학교_중복이_적은_지원자를_우선한다(MatchingType type) {
        // given
        MatchingSchoolContext schoolContext = new MatchingSchoolContext(Map.of(10L, 1L, 1L, 1L, 2L, 2L));
        schoolContext.addMember(PROJECT_ID, 10L);
        List<ProjectApplication> applicants = List.of(
            application(1L, ProjectApplicationStatus.REJECTED), application(2L, ProjectApplicationStatus.REJECTED));

        // when
        AutoDecisionResult result = policy(type).decideAutomatically(applicants, 2, new Random(42), schoolContext);

        // then
        assertThat(result.approvedIds()).containsExactly(2L);
    }

    @ParameterizedTest
    @EnumSource(MatchingType.class)
    void 같은_학교_배정이_불가피해도_최소_인원은_선발한다(MatchingType type) {
        // given
        MatchingSchoolContext schoolContext = new MatchingSchoolContext(Map.of(10L, 1L, 1L, 1L, 2L, 1L));
        schoolContext.addMember(PROJECT_ID, 10L);
        List<ProjectApplication> applicants = List.of(
            application(1L, ProjectApplicationStatus.SUBMITTED), application(2L, ProjectApplicationStatus.SUBMITTED));

        // when
        AutoDecisionResult result = policy(type).decideAutomatically(applicants, 2, new Random(42), schoolContext);

        // then
        assertThat(result.approvedIds()).hasSize(1).isSubsetOf(1L, 2L);
        assertThat(result.rejectedIds()).hasSize(1).doesNotContainAnyElementsOf(result.approvedIds());
    }

    @Test
    void 이번에_선발한_인원의_학교를_다음_선발에_반영한다() {
        // given
        Map<Long, Long> schoolIds = Map.of(1L, 2L, 2L, 1L, 3L, 2L, 4L, 1L);
        MatchingSchoolContext schoolContext = new MatchingSchoolContext(schoolIds);
        List<ProjectApplication> applicants = List.of(
            application(1L, ProjectApplicationStatus.SUBMITTED), application(2L, ProjectApplicationStatus.SUBMITTED),
            application(3L, ProjectApplicationStatus.SUBMITTED), application(4L, ProjectApplicationStatus.SUBMITTED));

        // when
        AutoDecisionResult result = new DeveloperMatchingPolicy().decideAutomatically(applicants, 4, new Random(42), schoolContext);

        // then
        assertThat(result.approvedIds()).hasSize(2);
        assertThat(result.approvedIds().stream().map(schoolIds::get).collect(Collectors.toSet())).hasSize(2);
    }

    @ParameterizedTest
    @CsvSource({"0, 2", "1, 1"})
    void 학교_인원수가_같으면_랜덤_선택에_따라_선발한다(int randomIndex, Long expectedApplicationId) {
        // given
        Random random = mock(Random.class);
        given(random.nextInt(anyInt())).willReturn(randomIndex);
        MatchingSchoolContext schoolContext = new MatchingSchoolContext(Map.of(1L, 1L, 2L, 2L));
        List<ProjectApplication> applicants = List.of(
            application(1L, ProjectApplicationStatus.SUBMITTED), application(2L, ProjectApplicationStatus.SUBMITTED));

        // when
        AutoDecisionResult result = new DeveloperMatchingPolicy().decideAutomatically(applicants, 2, random, schoolContext);

        // then
        assertThat(result.approvedIds()).containsExactly(expectedApplicationId);
    }

    private MatchingDecisionPolicy policy(MatchingType type) {
        return switch (type) {
            case PLAN_DESIGN -> new DesignerMatchingPolicy();
            case PLAN_DEVELOPER -> new DeveloperMatchingPolicy();
        };
    }

    private ProjectApplication application(Long memberId, ProjectApplicationStatus status) {
        Project project = Project.createDraft(1L, 1L, 10L, 1L, 10L);
        ReflectionTestUtils.setField(project, "id", PROJECT_ID);
        Instant start = Instant.parse("2026-10-01T00:00:00Z");
        ProjectMatchingRound round = ProjectMatchingRound.create("개발 1차", null,
            MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST, 1L,
            start, start.plusSeconds(3600), start.plusSeconds(7200), start.plusSeconds(10800));
        ProjectApplication application = ProjectApplication.create(ProjectApplicationForm.create(project, 10L), 1L, memberId, round);
        ReflectionTestUtils.setField(application, "id", memberId);
        ReflectionTestUtils.setField(application, "status", status);
        return application;
    }
}
