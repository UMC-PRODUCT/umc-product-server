package com.umc.product.test.domain.rehearsal;

import static com.umc.product.project.domain.enums.ProjectApplicationStatus.APPROVED;
import static com.umc.product.project.domain.enums.ProjectApplicationStatus.REJECTED;
import static com.umc.product.project.domain.enums.ProjectApplicationStatus.SUBMITTED;
import static com.umc.product.test.domain.rehearsal.RehearsalSnapshots.WEB;
import static com.umc.product.test.domain.rehearsal.RehearsalSnapshots.application;
import static com.umc.product.test.domain.rehearsal.RehearsalSnapshots.project;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.test.domain.rehearsal.DecisionPlan.PlannedDecision;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.ApplicationRow;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.PartSlot;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.ProjectSlot;

@DisplayName("봇 PM 결정 계획")
class BotDecisionPlannerTest {

    private static final MinimumRule DEVELOPER = (applicants, remaining) -> applicants >= remaining
        ? (int) Math.ceil(remaining * 0.5) : applicants * 2 > remaining ? (int) Math.ceil(remaining * 0.25) : 0;
    private static final BotDecisionParams NO_JITTER = new BotDecisionParams(null, 0, Set.of(), null);
    private static final ChapterRoles NO_ROLES = new ChapterRoles(Set.of(), List.of());

    @Test
    @DisplayName("1차 TO 6·지원 9면 올림(0.6 × 6) = 4명 합격, 남은 5명 중 2명 불합격, 3명은 미결정으로 두고 합격을 먼저 한다")
    void approvesTargetThenRejectsHalfOfRest() {
        RoundSnapshot snapshot = RehearsalSnapshots.snapshot(RehearsalSnapshots.decisionOpen(), List.of(),
            List.of(project(1, "po1@qa.test", 6)), submitted(1, 9));

        DecisionPlan plan = BotDecisionPlanner.plan(snapshot, NO_ROLES, NO_JITTER, DEVELOPER);

        assertThat(plan.approvals()).hasSize(4);
        assertThat(plan.rejections()).hasSize(2);
        assertThat(Stream.concat(plan.approvals().stream(), plan.rejections().stream()))
            .extracting(PlannedDecision::decidedBy).containsOnly(1001L);
    }

    @Test
    @DisplayName("목표는 최소 선발 인원 아래로 내려가지 않고, 이미 채운 인원을 빼며, 남은 TO와 지원자 수를 넘지 않는다")
    void targetFormula() {
        assertThat(BotDecisionPlanner.target(new PartSlot(6, 0, 0), 9, 0.6 * 1.1, DEVELOPER)).isEqualTo(4);
        assertThat(BotDecisionPlanner.target(new PartSlot(6, 3, 0), 9, 0.6, DEVELOPER)).isEqualTo(2);
        assertThat(BotDecisionPlanner.target(new PartSlot(4, 0, 0), 2, 1.0, DEVELOPER)).isEqualTo(2);
        assertThat(BotDecisionPlanner.target(new PartSlot(5, 0, 0), 9, 0.6, DEVELOPER)).isEqualTo(3);
        assertThat(BotDecisionPlanner.target(new PartSlot(4, 4, 0), 3, 1.0, DEVELOPER)).isZero();
    }

    @Test
    @DisplayName("QA 프로젝트와 제외 프로젝트는 결정하지 않고, 이미 결정된 지원서는 바꾸지 않으며, 다시 계획하면 결정이 없다")
    void skipsQaProjectsKeepsHumanDecisionsAndConverges() {
        List<ProjectSlot> projects = List.of(project(1, "po1@qa.test", 6), project(2, "qa@qa.test", 6),
            project(3, "po3@qa.test", 6));
        List<ApplicationRow> applications = new ArrayList<>(submitted(1, 9));
        applications.addAll(submitted(2, 3).stream().map(row -> withProject(row, 2)).toList());
        applications.addAll(submitted(3, 3).stream().map(row -> withProject(row, 3)).toList());
        applications.set(0, withStatus(applications.get(0), REJECTED));
        ChapterRoles roles = new ChapterRoles(Set.of("qa@qa.test"), List.of());
        BotDecisionParams params = new BotDecisionParams(null, 0, Set.of(3L), null);
        RoundSnapshot before = RehearsalSnapshots.snapshot(RehearsalSnapshots.decisionOpen(), List.of(), projects,
            applications);

        DecisionPlan plan = BotDecisionPlanner.plan(before, roles, params, DEVELOPER);

        List<PlannedDecision> decisions = Stream.concat(plan.approvals().stream(), plan.rejections().stream())
            .toList();
        assertThat(decisions).extracting(PlannedDecision::projectId).containsOnly(1L);
        assertThat(decisions).extracting(PlannedDecision::applicationId).doesNotContain(applications.get(0)
            .applicationId());
        assertThat(plan.approvals()).hasSize(4);
        assertThat(plan.rejections()).hasSize(1);
        assertThat(plan.skipped()).containsEntry(SkipReason.QA_PROJECT, 1).containsEntry(SkipReason.EXCLUDED, 1)
            .containsEntry(SkipReason.ALREADY_DECIDED, 1);

        Map<Long, ProjectApplicationStatus> decided = new java.util.HashMap<>();
        plan.approvals().forEach(decision -> decided.put(decision.applicationId(), APPROVED));
        plan.rejections().forEach(decision -> decided.put(decision.applicationId(), REJECTED));
        List<ApplicationRow> afterRows = applications.stream()
            .map(row -> decided.containsKey(row.applicationId()) ? withStatus(row, decided.get(row.applicationId())) : row)
            .toList();
        RoundSnapshot after = RehearsalSnapshots.snapshot(RehearsalSnapshots.decisionOpen(), List.of(), projects,
            afterRows);
        DecisionPlan replanned = BotDecisionPlanner.plan(after, roles, params, DEVELOPER);
        assertThat(replanned.approvals()).isEmpty();
        assertThat(replanned.rejections()).isEmpty();
    }

    @Test
    @DisplayName("지터는 PO마다 고정이고 0.8~1.2배 안에 있다")
    void jitterIsFixedPerOwnerWithinRange() {
        LongStream.range(0, 200).mapToObj(index -> "po" + index + "@qa.test").forEach(owner -> {
            double factor = 1 + 0.2 * (2 * SeedHash.unit(SeedHash.FIXED_SEED, SeedHash.Purpose.JITTER, owner) - 1);
            assertThat(factor).isBetween(0.8, 1.2);
        });
    }

    private static List<ApplicationRow> submitted(long firstApplicant, int count) {
        return LongStream.range(0, count)
            .mapToObj(index -> application(firstApplicant * 100 + index, 1, firstApplicant * 100 + index, SUBMITTED,
                true))
            .toList();
    }

    private static ApplicationRow withProject(ApplicationRow row, long projectId) {
        return new ApplicationRow(row.applicationId(), projectId, row.applicantMemberId(), row.applicantEmail(), WEB,
            row.status(), row.botAnswered());
    }

    private static ApplicationRow withStatus(ApplicationRow row, ProjectApplicationStatus status) {
        return new ApplicationRow(row.applicationId(), row.projectId(), row.applicantMemberId(), row.applicantEmail(),
            WEB, status, row.botAnswered());
    }
}
