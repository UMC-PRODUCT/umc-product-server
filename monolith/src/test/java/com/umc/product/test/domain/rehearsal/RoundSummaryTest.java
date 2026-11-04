package com.umc.product.test.domain.rehearsal;

import static com.umc.product.project.domain.enums.ProjectApplicationStatus.APPROVED;
import static com.umc.product.project.domain.enums.ProjectApplicationStatus.DRAFT;
import static com.umc.product.project.domain.enums.ProjectApplicationStatus.REJECTED;
import static com.umc.product.project.domain.enums.ProjectApplicationStatus.SUBMITTED;
import static com.umc.product.test.domain.rehearsal.RehearsalSnapshots.WEB;
import static com.umc.product.test.domain.rehearsal.RehearsalSnapshots.application;
import static com.umc.product.test.domain.rehearsal.RehearsalSnapshots.candidate;
import static com.umc.product.test.domain.rehearsal.RehearsalSnapshots.project;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.test.domain.rehearsal.ChapterRoles.ReservedSeat;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.Candidate;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.PartSlot;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.ProjectSlot;
import com.umc.product.test.domain.rehearsal.RoundSummary.PartRow;

@DisplayName("차수 요약")
class RoundSummaryTest {

    @Test
    @DisplayName("프로젝트·파트별로 제출한 지원을 봇·QA로 나누고 합격·불합격·미결정과 채움률을 센다")
    void countsApplicationsPerProjectPart() {
        ProjectSlot botProject = new ProjectSlot(1L, "봇", 11L, "po1@qa.test", Map.of(WEB, new PartSlot(4, 1, 0)));
        RoundSnapshot snapshot = RehearsalSnapshots.snapshot(RehearsalSnapshots.decisionOpen(),
            List.of(candidate(1), candidate(2), candidate(3), candidate(4), candidate(5)),
            List.of(botProject, project(2, "qa@qa.test", 3), project(3, "po3@qa.test", 2)),
            List.of(application(1, 1, 1, APPROVED, true), application(2, 1, 2, REJECTED, true),
                application(3, 1, 3, SUBMITTED, true), application(4, 2, 4, SUBMITTED, false),
                application(5, 3, 5, DRAFT, true)));
        ChapterRoles roles = new ChapterRoles(Set.of("qa@qa.test"),
            List.of(new ReservedSeat("m4@qa.test", WEB, 'A')));

        RoundSummary summary = RoundSummary.of(snapshot, roles);

        PartRow bot = summary.projects().getFirst().parts().getFirst();
        assertThat(bot).isEqualTo(new PartRow(WEB, 4, 1, 0, 3, 0, 1, 1, 1, 0.5));
        assertThat(summary.projects().get(1).qaProject()).isTrue();
        assertThat(summary.projects().get(1).parts().getFirst().appliedQa()).isEqualTo(1);
        assertThat(summary.underfilledProjects()).containsExactly(1L, 2L, 3L);
        assertThat(summary.projectsWithoutApplicants()).containsExactly(3L);
        assertThat(summary.parts()).first().satisfies(part -> {
            assertThat(part.part()).isEqualTo(WEB);
            assertThat(part.eligible()).isEqualTo(5);
            assertThat(part.applied()).isEqualTo(4);
            assertThat(part.notApplied()).isEqualTo(1);
        });
        assertThat(summary.qaReserved()).singleElement().satisfies(row -> {
            assertThat(row.label()).isEqualTo('A');
            assertThat(row.applicationStatus()).isEqualTo(SUBMITTED);
            assertThat(row.projectId()).isEqualTo(2L);
        });
        assertThat(summary.round().stage()).isEqualTo(RoundStage.DECISION_WINDOW);
    }

    @Test
    @DisplayName("자동 선발 뒤 채움률은 지원서 합격 수 대신 이 차수가 선발한 팀원 수로 센다")
    void fillRateUsesSelectedMembersAfterAutoDecision() {
        RoundSnapshot.Round decided = new RoundSnapshot.Round("쿼카 개발", 3L, "쿼카",
            RehearsalSnapshots.decisionOpen().type(), new RoundTimes(77L, MatchingPhase.FIRST,
            RehearsalSnapshots.NOW.minusSeconds(9000), RehearsalSnapshots.NOW.minusSeconds(7200),
            RehearsalSnapshots.NOW.minusSeconds(60), RehearsalSnapshots.NOW.plusSeconds(3600),
            RehearsalSnapshots.NOW.minusSeconds(30), null));
        ProjectSlot slot = new ProjectSlot(1L, "봇", 11L, "po1@qa.test", Map.of(WEB, new PartSlot(4, 0, 4)));
        RoundSnapshot snapshot = RehearsalSnapshots.snapshot(decided, List.<Candidate>of(), List.of(slot),
            List.of(application(1, 1, 1, SUBMITTED, true)));

        RoundSummary summary = RoundSummary.of(snapshot, new ChapterRoles(Set.of(), List.of()));

        assertThat(summary.projects().getFirst().parts().getFirst().fillRate()).isEqualTo(1.0);
        assertThat(summary.underfilledProjects()).isEmpty();
        assertThat(summary.round().stage()).isEqualTo(RoundStage.AWAITING_PUBLISH);
    }

    @Test
    @DisplayName("1~3차 지금 단계는 지원 전, 지원 중, 결정 중, 자동 선발 대기 순서로 시각에서 나온다")
    void stageFollowsTimes() {
        RoundTimes round = RehearsalSnapshots.applicationOpen().times();

        assertThat(round.stageAt(round.startsAt().minusSeconds(1))).isEqualTo(RoundStage.BEFORE_START);
        assertThat(round.stageAt(round.endsAt())).isEqualTo(RoundStage.APPLICATION_OPEN);
        assertThat(round.stageAt(round.endsAt().plusSeconds(1))).isEqualTo(RoundStage.DECISION_WINDOW);
        assertThat(round.stageAt(round.decisionDeadline().plusSeconds(1))).isEqualTo(RoundStage.AWAITING_AUTO_DECISION);
        assertThat(round.isApplicationOpenAt(round.endsAt())).isTrue();
        assertThat(round.isDecisionOpenAt(round.endsAt())).isFalse();
        assertThat(round.isDecisionOpenAt(round.decisionDeadline())).isTrue();
    }
}
