package com.umc.product.test.domain.rehearsal;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.ApplicationRow;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.Candidate;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.PartSlot;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.ProjectSlot;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.Round;

/**
 * 순수 계획·요약 테스트용 스냅샷 조립기.
 */
final class RehearsalSnapshots {
    static final Instant NOW = Instant.parse("2026-11-04T10:00:00Z");
    static final ChallengerPart WEB = ChallengerPart.WEB_PRODUCT_ENGINEER;

    private RehearsalSnapshots() {
    }

    static Round developerRound(MatchingPhase phase, Instant startsAt, Instant endsAt, Instant decisionDeadline) {
        return new Round(77L, "쿼카 개발", 3L, "쿼카", MatchingType.PLAN_DEVELOPER, phase, startsAt, endsAt,
            decisionDeadline, decisionDeadline.plus(Duration.ofHours(1)), null, null);
    }

    static Round applicationOpen() {
        return developerRound(MatchingPhase.FIRST, NOW.minusSeconds(60), NOW.plusSeconds(3600),
            NOW.plusSeconds(7200));
    }

    static Round decisionOpen() {
        return developerRound(MatchingPhase.FIRST, NOW.minusSeconds(7200), NOW.minusSeconds(60),
            NOW.plusSeconds(3600));
    }

    static RoundSnapshot snapshot(Round round, List<Candidate> candidates, List<ProjectSlot> projects,
                                  List<ApplicationRow> applications) {
        return new RoundSnapshot(round, NOW, candidates, projects, applications, Map.of());
    }

    static RoundSnapshot snapshot(Round round, List<Candidate> candidates, List<ProjectSlot> projects,
                                  List<ApplicationRow> applications, Map<Long, Set<Long>> rejectedLastRound) {
        return new RoundSnapshot(round, NOW, candidates, projects, applications, rejectedLastRound);
    }

    static Candidate candidate(long id) {
        return new Candidate(id, "m" + id + "@qa.test", WEB, false);
    }

    static ProjectSlot project(long id, String owner, int quota) {
        return new ProjectSlot(id, "p" + id, 1000 + id, owner, Map.of(WEB, new PartSlot(quota, 0, 0)));
    }

    static ApplicationRow application(long id, long projectId, long applicantId, ProjectApplicationStatus status,
                                      boolean bot) {
        return new ApplicationRow(id, projectId, applicantId, "m" + applicantId + "@qa.test", WEB, status, bot);
    }
}
