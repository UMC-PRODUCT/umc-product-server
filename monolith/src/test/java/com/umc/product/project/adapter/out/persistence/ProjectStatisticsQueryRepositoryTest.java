package com.umc.product.project.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
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
    void 공개_팀원_통계는_PENDING_PUBLICATION_멤버를_제외한다() {
        // given
        Project project = project();
        em.persist(ProjectMember.create(project, 10L, ChallengerPart.WEB_PRODUCT_ENGINEER, 999L));
        em.persist(ProjectMember.createForMatching(project, 11L, ChallengerPart.WEB_PRODUCT_ENGINEER, round(MatchingPhase.FIRST), 999L));
        em.flush();
        em.clear();

        // when
        var result = sut.listPublicActiveMembersByChapterId(1L);

        // then
        assertThat(result)
            .extracting(row -> row.memberId()).containsExactly(10L);
    }

    @Test
    void APPROVED_지원서_통계는_결과가_발표된_차수만_포함한다() {
        // given
        Project project = project();
        ProjectApplicationForm form = em.persist(ProjectApplicationForm.create(project, 999L));
        ProjectMatchingRound published = round(MatchingPhase.FIRST);
        published.publishResult(Instant.now());
        ProjectMatchingRound pending = round(MatchingPhase.SECOND);
        ProjectApplication approved = em.persist(ProjectApplication.create(form, 100L, 10L, published));
        ReflectionTestUtils.setField(approved, "status", ProjectApplicationStatus.APPROVED);
        ProjectApplication hidden = em.persist(ProjectApplication.create(form, 101L, 11L, pending));
        ReflectionTestUtils.setField(hidden, "status", ProjectApplicationStatus.APPROVED);
        em.flush();
        em.clear();

        // when
        var result = sut.listApprovedApplicationsByProjectIds(Set.of(project.getId()));

        // then
        assertThat(result)
            .extracting(row -> row.applicantMemberId()).containsExactly(10L);
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
