package com.umc.product.test.domain.rehearsal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.umc.product.test.domain.rehearsal.ApplyPlan.PlannedApplication;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.ApplicationRow;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.Candidate;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.PartSlot;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.ProjectSlot;
import com.umc.product.test.domain.rehearsal.SeedHash.Purpose;

/**
 * 같은 스냅샷 사실이면 몇 번을 불러도 같은 계획이 나온다.
 * <p>
 * 대상 풀은 "팀 없음, QA 몫 아님, 제외 아님"으로 고정한다. "이 차수 지원서 없음"으로 거르면 두 번째 호출에서 풀이 줄어
 * 다른 80%를 뽑게 되기 때문이다. 풀에서 해시 순위 상위 {@code round(applyRate × 풀)}명을 고르고, 풀에서 이미 지원한
 * 사람을 빼고 남은 자리만큼만 새로 지원시킨다. 그래서 다시 불러도 지원자는 그 수를 넘지 않는다.
 */
public final class BotApplyPlanner {
    /** 로그정규 인기도의 σ. 1.0이면 상위 20% 프로젝트가 지원의 절반 안팎을 받는다. */
    static final double POPULARITY_SIGMA = 1.0;

    private BotApplyPlanner() {
    }

    public static ApplyPlan plan(RoundSnapshot snapshot, ChapterRoles roles, BotApplyParams params) {
        long seed = params.seedFor(snapshot.round());
        Set<String> reserved = roles.reservedEmails();
        Map<SkipReason, Integer> skipped = new EnumMap<>(SkipReason.class);
        List<Candidate> pool = new ArrayList<>();
        for (Candidate candidate : snapshot.candidates()) {
            Optional<SkipReason> reason = candidate.hasTeam() ? Optional.of(SkipReason.HAS_TEAM)
                : reserved.contains(candidate.email()) ? Optional.of(SkipReason.QA_RESERVED)
                : params.excludeMemberIds().contains(candidate.memberId()) ? Optional.of(SkipReason.EXCLUDED)
                : Optional.empty();
            reason.ifPresentOrElse(value -> skipped.merge(value, 1, Integer::sum), () -> pool.add(candidate));
        }
        int target = (int) Math.round(params.applyRate() * pool.size());
        List<Candidate> ranked = pool.stream()
            .sorted(Comparator.comparingDouble((Candidate c) -> SeedHash.unit(seed, Purpose.APPLY_SELECT, c.email()))
                .thenComparing(Candidate::memberId))
            .toList();
        if (pool.size() > target) {
            skipped.put(SkipReason.NOT_SELECTED, pool.size() - target);
        }

        Set<Long> submitted = snapshot.applications().stream().filter(ApplicationRow::submitted)
            .map(ApplicationRow::applicantMemberId).collect(Collectors.toSet());
        Map<Long, ApplicationRow> drafts = snapshot.applications().stream().filter(row -> !row.submitted())
            .collect(Collectors.toMap(ApplicationRow::applicantMemberId, row -> row, (a, b) -> a));
        int budget = target - (int) pool.stream().filter(c -> submitted.contains(c.memberId())).count();
        List<ProjectSlot> projects = snapshot.projects().stream()
            .sorted(Comparator.comparing(ProjectSlot::ownerEmail)).toList();

        List<PlannedApplication> planned = new ArrayList<>();
        for (Candidate candidate : ranked.subList(0, target)) {
            if (submitted.contains(candidate.memberId())) {
                skipped.merge(SkipReason.ALREADY_APPLIED, 1, Integer::sum);
                continue;
            }
            if (planned.size() >= budget) {
                skipped.merge(SkipReason.NOT_SELECTED, 1, Integer::sum);
                continue;
            }
            ApplicationRow draft = drafts.get(candidate.memberId());
            if (draft != null) {
                planned.add(new PlannedApplication(candidate.memberId(), candidate.email(), draft.projectId(),
                    candidate.part(), draft.applicationId()));
                continue;
            }
            Set<Long> rejected = snapshot.rejectedLastRound().getOrDefault(candidate.memberId(), Set.of());
            List<ProjectSlot> eligible = projects.stream()
                .filter(project -> !rejected.contains(project.projectId()))
                .filter(project -> Optional.ofNullable(project.parts().get(candidate.part()))
                    .map(PartSlot::remaining).orElse(0) > 0)
                .toList();
            if (eligible.isEmpty()) {
                skipped.merge(SkipReason.NO_ELIGIBLE_PROJECT, 1, Integer::sum);
                continue;
            }
            ProjectSlot picked = pick(eligible, SeedHash.unit(seed, Purpose.PROJECT_PICK, candidate.email()));
            planned.add(new PlannedApplication(candidate.memberId(), candidate.email(), picked.projectId(),
                candidate.part(), null));
        }
        return new ApplyPlan(planned, skipped);
    }

    /**
     * 프로젝트 ID 대신 PO 이메일과 고정 seed로 정해, 1·2·3차와 프로젝트 초기화 뒤에도 같다.
     */
    static double popularity(String ownerEmail) {
        double first = Math.max(Double.MIN_VALUE, SeedHash.unit(SeedHash.FIXED_SEED, Purpose.POPULARITY, ownerEmail, "r"));
        double second = SeedHash.unit(SeedHash.FIXED_SEED, Purpose.POPULARITY, ownerEmail, "theta");
        double normal = Math.sqrt(-2 * Math.log(first)) * Math.cos(2 * Math.PI * second);
        return Math.exp(POPULARITY_SIGMA * normal);
    }

    private static ProjectSlot pick(List<ProjectSlot> eligible, double unit) {
        double total = eligible.stream().mapToDouble(project -> popularity(project.ownerEmail())).sum();
        double threshold = unit * total;
        double cumulative = 0;
        for (ProjectSlot project : eligible) {
            cumulative += popularity(project.ownerEmail());
            if (threshold < cumulative) {
                return project;
            }
        }
        return eligible.getLast();
    }
}
