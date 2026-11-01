package com.umc.product.project.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

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
@Import(ProjectStatisticsQueryRepository.class)
class ProjectStatisticsQueryRepositoryTest {

    @Autowired
    TestEntityManager em;
    @Autowired
    ProjectStatisticsQueryRepository sut;

    @Test
    void 공개_팀원_통계는_차수가_있는_ACTIVE_디자인_개발_팀원만_조회한다() {
        // given
        Project project = project();
        ProjectMatchingRound first = round(MatchingPhase.FIRST);
        ProjectMember active = ProjectMember.createForMatching(project, 10L, ChallengerPart.WEB_PRODUCT_ENGINEER,
            first, 999L);
        active.publish(Instant.now());
        em.persist(active);
        em.persist(ProjectMember.create(project, 12L, ChallengerPart.WEB_PRODUCT_ENGINEER, 999L));
        ProjectMember plan = ProjectMember.createForMatching(project, 13L, ChallengerPart.PLAN, first, 999L);
        plan.publish(Instant.now());
        em.persist(plan);
        Project hidden = project();
        ReflectionTestUtils.setField(hidden, "status", ProjectStatus.DRAFT);
        ProjectMember hiddenMember = ProjectMember.createForMatching(hidden, 14L, ChallengerPart.WEB_PRODUCT_ENGINEER,
            first, 999L);
        hiddenMember.publish(Instant.now());
        em.persist(hiddenMember);
        em.flush();
        em.clear();

        // when
        var result = sut.listPublicActiveMembersByChapterId(1L);

        // then
        assertThat(result)
            .extracting(row -> row.memberId()).containsExactly(10L);
        assertThat(result.getFirst().matchingRoundId()).isEqualTo(first.getId());
    }

    @Test
    void 공개_팀원_통계는_발표_대기_팀원을_제외한다() {
        // given
        Project project = project();
        ProjectMatchingRound first = round(MatchingPhase.FIRST);
        ProjectMember active = ProjectMember.createForMatching(project, 10L, ChallengerPart.WEB_PRODUCT_ENGINEER,
            first, 999L);
        active.publish(Instant.now());
        em.persist(active);
        ProjectMember pending = ProjectMember.createForMatching(project, 11L, ChallengerPart.WEB_PRODUCT_ENGINEER,
            first, 999L);
        em.persist(pending);
        assertThat(pending.getStatus()).isEqualTo(ProjectMemberStatus.PENDING_PUBLICATION);
        em.flush();
        em.clear();

        // when
        var result = sut.listPublicActiveMembersByChapterId(1L);

        // then
        assertThat(result).extracting(row -> row.memberId()).containsExactly(10L);
    }

    @Test
    void 공개_프로젝트는_등록순으로_조회하고_비공개_상태는_제외한다() {
        // given
        Project later = project();
        ReflectionTestUtils.setField(later, "submittedAt", Instant.parse("2026-10-02T00:00:00Z"));
        Project earlier = project();
        ReflectionTestUtils.setField(earlier, "submittedAt", Instant.parse("2026-10-01T00:00:00Z"));
        ReflectionTestUtils.setField(earlier, "status", ProjectStatus.COMPLETED);
        Project hidden = project();
        ReflectionTestUtils.setField(hidden, "status", ProjectStatus.PENDING_REVIEW);
        Project otherChapter = project();
        ReflectionTestUtils.setField(otherChapter, "chapterId", 2L);
        em.flush();
        em.clear();

        // when
        var result = sut.listPublicProjectsByChapterId(1L);

        // then
        assertThat(result).extracting(row -> row.projectId()).containsExactly(earlier.getId(), later.getId());
        assertThat(result).extracting(row -> row.projectName()).containsOnly("매칭 프로젝트");
    }

    private Project project() {
        Project project = Project.createDraft(1L, 1L, 100L, 1L, 100L);
        ReflectionTestUtils.setField(project, "status", ProjectStatus.IN_PROGRESS);
        ReflectionTestUtils.setField(project, "name", "매칭 프로젝트");
        return em.persist(project);
    }

    private ProjectMatchingRound round(MatchingPhase phase) {
        Instant publishAt = Instant.now().minusSeconds(60);
        ProjectMatchingRound round = ProjectMatchingRound.create("개발 1차", null, MatchingType.PLAN_DEVELOPER,
            phase, 1L, publishAt.minusSeconds(14400), publishAt.minusSeconds(7200),
            publishAt.minusSeconds(3600), publishAt);
        round.executeAutoDecision(999L);
        return em.persist(round);
    }
}
