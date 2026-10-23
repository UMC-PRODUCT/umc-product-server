package com.umc.product.project.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
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
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.support.PersistenceAdapterTest;

@PersistenceAdapterTest
@Import({ProjectMemberPersistenceAdapter.class, ProjectMemberQueryRepository.class})
class ProjectMemberPersistenceAdapterTest {

    @Autowired
    TestEntityManager em;
    @Autowired
    ProjectMemberPersistenceAdapter sut;

    Project project;
    ProjectMatchingRound round;
    ProjectMember active;
    ProjectMember pending;

    @BeforeEach
    void setUp() {
        project = project();
        round = round();
        active = ProjectMember.createForMatching(project, 10L, ChallengerPart.WEB_PRODUCT_ENGINEER, round, 999L);
        active.publish(Instant.now());
        em.persist(active);
        pending = em.persist(ProjectMember.createForMatching(
            project, 11L, ChallengerPart.WEB_PRODUCT_ENGINEER, round, 999L));
        ProjectMember withdrawn = em.persist(ProjectMember.create(project, 12L, ChallengerPart.WEB_PRODUCT_ENGINEER, 999L));
        withdrawn.withdraw("탈퇴", 999L);
        ProjectMember completed = em.persist(ProjectMember.create(project, 13L, ChallengerPart.WEB_PRODUCT_ENGINEER, 999L));
        completed.complete(999L);
        ProjectMember dismissed = em.persist(ProjectMember.create(project, 14L, ChallengerPart.WEB_PRODUCT_ENGINEER, 999L));
        dismissed.dismiss("해제", 999L);
        em.flush();
        em.clear();
    }

    @Test
    void 내부_팀원_조회는_활성과_발표_대기_멤버만_포함한다() {
        // when
        var result = sut.listActiveAndPendingPublicationByProjectId(project.getId());

        // then
        assertThat(result).extracting(ProjectMember::getId).containsExactly(active.getId(), pending.getId());
    }

    @Test
    void 공개_팀원_조회는_활성_멤버만_포함한다() {
        // when
        var members = sut.listByProjectId(project.getId());
        var membersByProject = sut.listByProjectIds(Set.of(project.getId()));

        // then
        assertThat(members).extracting(ProjectMember::getId).containsExactly(active.getId());
        assertThat(membersByProject.get(project.getId()))
            .extracting(ProjectMember::getId).containsExactly(active.getId());
    }

    @Test
    void 내부_정원은_발표_대기를_포함하고_공개_인원수는_활성_멤버만_집계한다() {
        // when
        var assigned = sut.countAssignedByProjectIdGroupByPart(project.getId());
        var published = sut.countByProjectIdGroupByPart(project.getId());

        // then
        assertThat(assigned).containsEntry(ChallengerPart.WEB_PRODUCT_ENGINEER, 2L);
        assertThat(published).containsEntry(ChallengerPart.WEB_PRODUCT_ENGINEER, 1L);
    }

    @Test
    void 중복_배정_검사는_해당_기수의_활성과_발표_대기_멤버만_포함한다() {
        // when
        var assignedIds = sut.listAssignedMemberIdsByGisuId(1L);

        // then
        assertThat(assignedIds).containsExactlyInAnyOrder(10L, 11L);
        assertThat(sut.existsByGisuAndMember(1L, 10L)).isTrue();
        assertThat(sut.existsByGisuAndMember(1L, 11L)).isTrue();
        assertThat(sut.existsByGisuAndMember(1L, 12L)).isFalse();
        assertThat(sut.listAssignedMemberIdsByGisuId(2L)).isEmpty();
    }

    @Test
    void 매칭_차수_조회는_발표된_멤버만_포함한다() {
        // when
        var result = sut.listMatchedRoundsByProjectIdsAndMemberIds(
            Set.of(project.getId()), Set.of(10L, 11L));

        // then
        assertThat(result).extracting(row -> row.memberId()).containsExactly(10L);
        assertThat(result).extracting(row -> row.matchingRoundId()).containsExactly(round.getId());
    }

    private Project project() {
        Project project = Project.createDraft(1L, 1L, 100L, 1L, 100L);
        ReflectionTestUtils.setField(project, "status", ProjectStatus.IN_PROGRESS);
        ReflectionTestUtils.setField(project, "name", "매칭 프로젝트");
        return em.persist(project);
    }

    private ProjectMatchingRound round() {
        Instant publishAt = Instant.now().minusSeconds(60);
        ProjectMatchingRound round = ProjectMatchingRound.create("개발 1차", null, MatchingType.PLAN_DEVELOPER,
            MatchingPhase.FIRST, 1L, publishAt.minusSeconds(14400), publishAt.minusSeconds(7200),
            publishAt.minusSeconds(3600), publishAt);
        round.executeAutoDecision(999L);
        return em.persist(round);
    }
}
