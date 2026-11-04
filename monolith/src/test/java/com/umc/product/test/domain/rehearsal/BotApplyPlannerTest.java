package com.umc.product.test.domain.rehearsal;

import static com.umc.product.project.domain.enums.ProjectApplicationStatus.DRAFT;
import static com.umc.product.project.domain.enums.ProjectApplicationStatus.SUBMITTED;
import static com.umc.product.test.domain.rehearsal.RehearsalSnapshots.WEB;
import static com.umc.product.test.domain.rehearsal.RehearsalSnapshots.application;
import static com.umc.product.test.domain.rehearsal.RehearsalSnapshots.candidate;
import static com.umc.product.test.domain.rehearsal.RehearsalSnapshots.project;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.test.domain.rehearsal.ApplyPlan.PlannedApplication;
import com.umc.product.test.domain.rehearsal.ChapterRoles.ReservedSeat;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.ApplicationRow;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.Candidate;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.ProjectSlot;
import com.umc.product.test.domain.roster.QaCastFixtures;
import com.umc.product.test.domain.roster.RehearsalCast;
import com.umc.product.test.domain.roster.RosterKey;

@DisplayName("봇 지원 계획")
class BotApplyPlannerTest {

    private static final ChapterRoles NO_ROLES = new ChapterRoles(Set.of(), List.of());

    @Test
    @DisplayName("팀 없는 사람 중 QA 몫과 제외 대상을 빼고 정확히 round(0.8 × 풀)명이 TO 남은 프로젝트에 지원한다")
    void appliesExactShareOfFixedPool() {
        List<Candidate> candidates = new ArrayList<>(LongStream.rangeClosed(1, 20).mapToObj(RehearsalSnapshots::candidate)
            .toList());
        candidates.add(new Candidate(21L, "m21@qa.test", WEB, true));
        ChapterRoles roles = new ChapterRoles(Set.of(), List.of(new ReservedSeat("m1@qa.test", WEB, 'A')));
        RoundSnapshot snapshot = RehearsalSnapshots.snapshot(RehearsalSnapshots.applicationOpen(), candidates,
            List.of(project(1, "po1@qa.test", 5), project(2, "po2@qa.test", 0)), List.of());

        ApplyPlan plan = BotApplyPlanner.plan(snapshot, roles, new BotApplyParams(0.8, Set.of(2L), null));

        assertThat(plan.applications()).hasSize(14);
        assertThat(plan.applications()).extracting(PlannedApplication::memberId).doesNotContain(1L, 2L, 21L);
        assertThat(plan.applications()).extracting(PlannedApplication::projectId).containsOnly(1L);
        assertThat(plan.skipped()).containsEntry(SkipReason.QA_RESERVED, 1).containsEntry(SkipReason.EXCLUDED, 1)
            .containsEntry(SkipReason.HAS_TEAM, 1).containsEntry(SkipReason.NOT_SELECTED, 4);
    }

    @Test
    @DisplayName("계획대로 지원한 뒤의 스냅샷으로 다시 계획하면 새 지원이 없다")
    void replanAfterExecutionIsEmpty() {
        List<Candidate> candidates = LongStream.rangeClosed(1, 30).mapToObj(RehearsalSnapshots::candidate).toList();
        List<ProjectSlot> projects = List.of(project(1, "po1@qa.test", 4), project(2, "po2@qa.test", 4));
        RoundSnapshot before = RehearsalSnapshots.snapshot(RehearsalSnapshots.applicationOpen(), candidates, projects,
            List.of());
        ApplyPlan first = BotApplyPlanner.plan(before, NO_ROLES, BotApplyParams.defaults());

        List<ApplicationRow> applied = first.applications().stream()
            .map(planned -> application(planned.memberId(), planned.projectId(), planned.memberId(), SUBMITTED, true))
            .toList();
        RoundSnapshot after = RehearsalSnapshots.snapshot(RehearsalSnapshots.applicationOpen(), candidates, projects,
            applied);
        ApplyPlan second = BotApplyPlanner.plan(after, NO_ROLES, BotApplyParams.defaults());

        assertThat(first.applications()).hasSize(24);
        assertThat(second.applications()).isEmpty();
        assertThat(second.skipped()).containsEntry(SkipReason.ALREADY_APPLIED, 24);
        assertThat(BotApplyPlanner.plan(before, NO_ROLES, BotApplyParams.defaults())).isEqualTo(first);
    }

    @Test
    @DisplayName("직전 차수에 떨어진 프로젝트는 고르지 않고, 이 차수 임시저장 지원서는 그 지원서를 이어 제출한다")
    void avoidsPreviouslyRejectedProjectAndResumesDraft() {
        List<Candidate> candidates = List.of(candidate(1), candidate(2));
        RoundSnapshot snapshot = RehearsalSnapshots.snapshot(RehearsalSnapshots.applicationOpen(), candidates,
            List.of(project(1, "po1@qa.test", 3), project(2, "po2@qa.test", 3)),
            List.of(application(50, 2, 2, DRAFT, false)), Map.of(1L, Set.of(1L)));

        ApplyPlan plan = BotApplyPlanner.plan(snapshot, NO_ROLES, new BotApplyParams(1.0, Set.of(), null));

        Map<Long, PlannedApplication> byMember = plan.applications().stream()
            .collect(Collectors.toMap(PlannedApplication::memberId, Function.identity()));
        assertThat(byMember.get(1L).projectId()).isEqualTo(2L);
        assertThat(byMember.get(2L).draftApplicationId()).isEqualTo(50L);
        assertThat(byMember.get(2L).projectId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("출발선 PO 59명의 인기도로 고르면 상위 20% 프로젝트가 지원의 절반 안팎을 받는다")
    void topFifthOfProjectsGetAboutHalfOfApplications() {
        RehearsalCast cast = QaCastFixtures.cast();
        List<RosterKey> owners = cast.chapters().stream().map(cast::startingLine)
            .flatMap(plan -> java.util.stream.Stream.concat(plan.qaOwners().stream(), plan.seededOwners().stream()))
            .toList();
        List<ProjectSlot> projects = LongStream.range(0, owners.size())
            .mapToObj(index -> project(index + 1, owners.get((int) index).email(), 10_000)).toList();
        List<Candidate> candidates = LongStream.rangeClosed(1, 5_000).mapToObj(RehearsalSnapshots::candidate).toList();
        RoundSnapshot snapshot = RehearsalSnapshots.snapshot(RehearsalSnapshots.applicationOpen(), candidates, projects,
            List.of());

        ApplyPlan plan = BotApplyPlanner.plan(snapshot, NO_ROLES, new BotApplyParams(1.0, Set.of(), null));

        Map<Long, Long> perProject = plan.applications().stream()
            .collect(Collectors.groupingBy(PlannedApplication::projectId, Collectors.counting()));
        long top = perProject.values().stream().sorted(Comparator.reverseOrder()).limit(owners.size() / 5)
            .mapToLong(Long::longValue).sum();
        assertThat(owners).hasSize(59);
        assertThat((double) top / plan.applications().size()).isBetween(0.4, 0.65);
    }
}
