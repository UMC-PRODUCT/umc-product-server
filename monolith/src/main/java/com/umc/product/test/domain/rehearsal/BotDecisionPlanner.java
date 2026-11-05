package com.umc.product.test.domain.rehearsal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.test.domain.rehearsal.DecisionPlan.PlannedDecision;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.ApplicationRow;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.PartSlot;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.ProjectSlot;
import com.umc.product.test.domain.rehearsal.SeedHash.Purpose;

/**
 * 이미 결정된 지원서(사람이 바꾼 것 포함)는 건드리지 않으므로, 다시 부르면 결정할 것이 남지 않는다.
 */
public final class BotDecisionPlanner {
    private static final double CEIL_TOLERANCE = 1e-9;

    private BotDecisionPlanner() {
    }

    public static DecisionPlan plan(RoundSnapshot snapshot, ChapterRoles roles, BotDecisionParams params,
                                    MinimumRule minimum) {
        long seed = params.seedFor(snapshot.round());
        double fillRate = params.fillRateFor(snapshot.round().times().phase());
        Map<SkipReason, Integer> skipped = new EnumMap<>(SkipReason.class);
        List<PlannedDecision> approvals = new ArrayList<>();
        List<PlannedDecision> rejections = new ArrayList<>();
        for (ProjectSlot project : snapshot.projects().stream()
            .sorted(Comparator.comparing(ProjectSlot::projectId)).toList()) {
            if (roles.qaProjectOwnerEmails().contains(project.ownerEmail())) {
                skipped.merge(SkipReason.QA_PROJECT, 1, Integer::sum);
                continue;
            }
            if (params.excludeProjectIds().contains(project.projectId())) {
                skipped.merge(SkipReason.EXCLUDED, 1, Integer::sum);
                continue;
            }
            double jitter = 1 + params.jitter()
                * (2 * SeedHash.unit(SeedHash.FIXED_SEED, Purpose.JITTER, project.ownerEmail()) - 1);
            for (ChallengerPart part : snapshot.round().parts().stream().sorted().toList()) {
                PartSlot slot = project.parts().get(part);
                List<ApplicationRow> applications = snapshot.applications().stream()
                    .filter(row -> row.projectId().equals(project.projectId()) && row.part() == part)
                    .filter(ApplicationRow::submitted)
                    .toList();
                if (slot == null || applications.isEmpty()) {
                    continue;
                }
                int target = target(slot, applications.size(), fillRate * jitter, minimum);
                int approved = count(applications, ProjectApplicationStatus.APPROVED);
                int rejected = count(applications, ProjectApplicationStatus.REJECTED);
                if (approved + rejected > 0) {
                    skipped.merge(SkipReason.ALREADY_DECIDED, approved + rejected, Integer::sum);
                }
                List<ApplicationRow> undecided = applications.stream()
                    .filter(row -> row.status() == ProjectApplicationStatus.SUBMITTED)
                    .sorted(Comparator.comparingDouble(
                            (ApplicationRow row) -> SeedHash.unit(seed, Purpose.DECISION_ORDER, row.applicantEmail()))
                        .thenComparing(ApplicationRow::applicationId))
                    .toList();
                int approve = Math.min(undecided.size(), Math.max(0, target - approved));
                int rejectTarget = (applications.size() - Math.max(target, approved)) / 2;
                int reject = Math.min(undecided.size() - approve, Math.max(0, rejectTarget - rejected));
                undecided.subList(0, approve).forEach(row -> approvals.add(decision(project, row)));
                undecided.subList(approve, approve + reject).forEach(row -> rejections.add(decision(project, row)));
            }
        }
        return new DecisionPlan(approvals, rejections, skipped);
    }

    static int target(PartSlot slot, int applicants, double fillFactor, MinimumRule minimum) {
        int wanted = (int) Math.ceil(fillFactor * slot.quota() - CEIL_TOLERANCE) - slot.assigned();
        int floor = minimum.minimumRequired(applicants, slot.remaining());
        return Math.max(0, Math.min(Math.max(wanted, floor), Math.min(slot.remaining(), applicants)));
    }

    private static PlannedDecision decision(ProjectSlot project, ApplicationRow row) {
        return new PlannedDecision(project.projectId(), row.applicationId(), project.ownerMemberId());
    }

    private static int count(List<ApplicationRow> applications, ProjectApplicationStatus status) {
        return (int) applications.stream().filter(row -> row.status() == status).count();
    }
}
