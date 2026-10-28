package com.umc.product.project.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectMemberStatus;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.support.PersistenceAdapterTest;

@PersistenceAdapterTest
@Import({ProjectMemberQueryRepository.class, ProjectQueryRepository.class})
class ProjectMatchingOverviewRepositoryTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    ProjectMemberQueryRepository sut;

    @Autowired
    ProjectQueryRepository projectQueryRepository;

    @Test
    void 지부의_공개_프로젝트만_제출순으로_조회한다() {
        // given
        Instant submittedAt = Instant.parse("2026-10-01T00:00:00Z");
        Project later = project(ProjectStatus.IN_PROGRESS);
        Project earlier = project(ProjectStatus.COMPLETED);
        Project sameTime = project(ProjectStatus.IN_PROGRESS);
        Project otherChapter = project(ProjectStatus.IN_PROGRESS);
        ReflectionTestUtils.setField(later, "submittedAt", submittedAt.plusSeconds(60));
        ReflectionTestUtils.setField(earlier, "submittedAt", submittedAt);
        ReflectionTestUtils.setField(sameTime, "submittedAt", submittedAt);
        ReflectionTestUtils.setField(otherChapter, "chapterId", 2L);
        project(ProjectStatus.DRAFT);
        project(ProjectStatus.PENDING_REVIEW);
        project(ProjectStatus.ABORTED);
        em.flush();
        em.clear();

        // when
        var result = projectQueryRepository.listPublicByChapterId(1L);

        // then
        assertThat(result).extracting(Project::getId)
            .containsExactly(earlier.getId(), sameTime.getId(), later.getId());
    }

    @Test
    void 조정_명단은_해제_예약자를_빼고_해당_차수_배정자를_포함한다() {
        // given
        Project project = project(ProjectStatus.IN_PROGRESS);
        ProjectMatchingRound random = round(MatchingPhase.RANDOM_MATCHING);
        ProjectMatchingRound first = round(MatchingPhase.FIRST);
        em.persist(ProjectMember.create(project, 1L, ChallengerPart.WEB, 999L));
        ProjectMember removal = ProjectMember.create(project, 2L, ChallengerPart.WEB, 999L);
        removal.scheduleRemoval(random, 999L, "해제 예약");
        em.persist(removal);
        em.persist(ProjectMember.createForMatching(project, 3L, ChallengerPart.WEB, random, 999L));
        em.persist(ProjectMember.createForMatching(project, 4L, ChallengerPart.WEB, first, 999L));
        em.flush();
        em.clear();

        // when
        var published = sut.listMatchingOverviewMembersByProjectIds(List.of(project.getId()), List.of());
        var adjustment = sut.listMatchingOverviewMembersByProjectIds(List.of(project.getId()), List.of(random.getId()));

        // then
        assertThat(published).extracting(ProjectMember::getMemberId).containsExactly(1L, 2L);
        assertThat(adjustment).extracting(ProjectMember::getMemberId).containsExactly(1L, 3L);
        assertThat(adjustment.getFirst().getAssignedMatchingRound()).isNull();
    }

    @Test
    void 정상_완료_팀원은_완료_프로젝트에서만_포함한다() {
        // given
        Project completed = project(ProjectStatus.COMPLETED);
        Project ongoing = project(ProjectStatus.IN_PROGRESS);
        Project aborted = project(ProjectStatus.ABORTED);
        Project hidden = project(ProjectStatus.DRAFT);
        member(completed, 10L, ProjectMemberStatus.COMPLETED);
        member(completed, 11L, ProjectMemberStatus.WITHDRAWN);
        member(completed, 12L, ProjectMemberStatus.DISMISSED);
        member(completed, 13L, ProjectMemberStatus.PENDING_PUBLICATION);
        member(completed, 14L, ProjectMemberStatus.ACTIVE);
        member(ongoing, 20L, ProjectMemberStatus.COMPLETED);
        member(ongoing, 21L, ProjectMemberStatus.ACTIVE);
        member(aborted, 30L, ProjectMemberStatus.ACTIVE);
        member(hidden, 40L, ProjectMemberStatus.ACTIVE);
        em.flush();
        em.clear();

        // when
        var result = sut.listMatchingOverviewMembersByProjectIds(
            List.of(completed.getId(), ongoing.getId(), aborted.getId(), hidden.getId()), List.of());

        // then
        assertThat(result).extracting(ProjectMember::getMemberId).containsExactly(10L, 21L);
    }

    private Project project(ProjectStatus status) {
        Project project = Project.createDraft(1L, 1L, 100L, 1L, 100L);
        ReflectionTestUtils.setField(project, "status", status);
        return em.persist(project);
    }

    private void member(Project project, Long memberId, ProjectMemberStatus status) {
        ProjectMember member = ProjectMember.create(project, memberId, ChallengerPart.WEB, 999L);
        ReflectionTestUtils.setField(member, "status", status);
        em.persist(member);
    }

    private ProjectMatchingRound round(MatchingPhase phase) {
        Instant now = Instant.now();
        Instant endsAt = now.plusSeconds(300);
        Instant deadline = phase == MatchingPhase.RANDOM_MATCHING ? endsAt : now.plusSeconds(600);
        ProjectMatchingRound round = ProjectMatchingRound.create("매칭", null, MatchingType.PLAN_DEVELOPER,
            phase, 1L, now.minusSeconds(300), endsAt, deadline, deadline.plusSeconds(300));
        round.executeAutoDecision(999L);
        return em.persist(round);
    }
}
