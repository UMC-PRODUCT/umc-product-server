package com.umc.product.project.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.query.dto.SearchProjectQuery;
import com.umc.product.project.application.service.command.ProjectMatchingResultPublicationCommandService;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.PartQuotaStatus;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.enums.ProjectMemberStatus;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.support.PersistenceAdapterTest;

@PersistenceAdapterTest
@Import({ProjectMemberPersistenceAdapter.class, ProjectMemberQueryRepository.class,
    ProjectMatchingRoundPersistenceAdapter.class, ProjectMatchingResultPublicationCommandService.class,
    ProjectQueryRepository.class, ProjectStatisticsQueryRepository.class,
    ProjectApplicationPersistenceAdapter.class, ProjectApplicationQueryRepository.class})
class ProjectMatchingResultPublicationTest {

    private final Map<Long, ProjectApplicationForm> forms = new java.util.HashMap<>();

    @Autowired
    TestEntityManager em;
    @Autowired
    ProjectMemberPersistenceAdapter members;
    @Autowired
    ProjectMatchingRoundPersistenceAdapter rounds;
    @Autowired
    ProjectMatchingResultPublicationCommandService publisher;
    @Autowired
    ProjectQueryRepository projects;
    @Autowired
    ProjectStatisticsQueryRepository statistics;

    @Test
    void 배정된_지원서는_공개할_때_합격으로_바꾸고_배정되지_않은_지원서는_불합격으로_확정한다() {
        // given
        Project project = project(100L);
        ProjectMatchingRound round = round(Instant.now().minusSeconds(600), true);
        ProjectMember selected = matchedMember(project, round, 10L);
        ProjectApplication selectedApplication = selected.getApplication();
        ReflectionTestUtils.setField(selectedApplication, "status", ProjectApplicationStatus.REJECTED);
        Instant pmDecidedAt = Instant.now().minusSeconds(3600).truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        ReflectionTestUtils.setField(selectedApplication, "statusChangedAt", pmDecidedAt);
        ProjectApplication unselected = application(project, round, 11L, ProjectApplicationStatus.SUBMITTED);
        ProjectApplication draft = application(project, round, 12L, ProjectApplicationStatus.DRAFT);
        ProjectApplication cancelled = application(project, round, 13L, ProjectApplicationStatus.CANCELLED);
        ProjectMatchingRound anotherRound = ProjectMatchingRound.create("다른 차수", null,
            MatchingType.PLAN_DEVELOPER, MatchingPhase.SECOND, 1L,
            Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200),
            Instant.now().plusSeconds(10800), Instant.now().plusSeconds(14400));
        em.persist(anotherRound);
        ProjectApplication other = application(project, anotherRound, 14L, ProjectApplicationStatus.SUBMITTED);
        em.flush();
        em.clear();
        assertThat(em.find(ProjectApplication.class, selectedApplication.getId()).getStatusChangedAt())
            .isEqualTo(pmDecidedAt);

        // when
        assertThat(publisher.publish(round.getId())).isTrue();
        em.flush();
        em.clear();

        // then
        ProjectApplication published = em.find(ProjectApplication.class, selectedApplication.getId());
        assertThat(published.getStatus()).isEqualTo(ProjectApplicationStatus.APPROVED);
        assertThat(published.getStatusChangedAt()).isAfter(pmDecidedAt);
        assertThat(published.getStatusChangeReason()).isEqualTo("auto-decide");
        assertThat(em.find(ProjectApplication.class, unselected.getId()).getStatus()).isEqualTo(ProjectApplicationStatus.REJECTED);
        assertThat(em.find(ProjectApplication.class, draft.getId()).getStatus()).isEqualTo(ProjectApplicationStatus.DRAFT);
        assertThat(em.find(ProjectApplication.class, cancelled.getId()).getStatus()).isEqualTo(ProjectApplicationStatus.CANCELLED);
        assertThat(em.find(ProjectApplication.class, other.getId()).getStatus()).isEqualTo(ProjectApplicationStatus.SUBMITTED);
        assertThat(em.find(ProjectMember.class, selected.getId()).getStatus()).isEqualTo(ProjectMemberStatus.ACTIVE);
        Instant processedAt = published.getStatusChangedAt();
        Instant publishedAt = em.find(ProjectMatchingRound.class, round.getId()).getResultPublishedAt();
        assertThat(publisher.publish(round.getId())).isFalse();
        em.flush();
        em.clear();
        assertThat(em.find(ProjectApplication.class, selectedApplication.getId()).getStatusChangedAt())
            .isEqualTo(processedAt);
        assertThat(em.find(ProjectMatchingRound.class, round.getId()).getResultPublishedAt()).isEqualTo(publishedAt);
        assertThat(members.listByProjectId(project.getId())).extracting(ProjectMember::getId)
            .containsExactly(selected.getId());
    }

    @Test
    void 지원서_없는_랜덤_배정도_발표할_때_팀원과_랜덤_매칭_조회에_반영된다() {
        // given
        Project project = project(100L);
        Instant startsAt = Instant.now().minusSeconds(86400);
        ProjectMatchingRound round = em.persist(ProjectMatchingRound.create(
            "개발자 4차", null, MatchingType.PLAN_DEVELOPER, MatchingPhase.FOURTH, 1L,
            startsAt, startsAt.plusSeconds(72000), startsAt.plusSeconds(72000), startsAt.plusSeconds(79200)));
        round.executeAutoDecision(null);
        ProjectMember member = em.persist(ProjectMember.createForMatching(
            project, 10L, ChallengerPart.WEB, round, 999L));
        em.flush();
        em.clear();

        assertThat(rounds.listOpenAt(1L, startsAt.plusSeconds(3600))).isEmpty();
        assertThat(members.listAssignedMemberIdsByGisuId(1L)).containsExactly(10L);
        assertThat(members.listAssignedMemberIdsByGisuId(2L)).isEmpty();
        assertThat(members.countAssignedByProjectIdGroupByPart(project.getId())).containsEntry(ChallengerPart.WEB, 1L);
        assertThat(members.listByProjectId(project.getId())).isEmpty();
        assertThat(members.findActiveWithoutApplicationByMemberIdAndGisuIdAndMatchingType(
            10L, 1L, MatchingType.PLAN_DEVELOPER)).isEmpty();
        assertThat(members.listMatchedRoundsByProjectIdsAndMemberIds(Set.of(project.getId()), Set.of(10L))).isEmpty();

        // when
        publisher.publish(round.getId());
        em.flush();
        em.clear();

        // then
        assertThat(members.listByProjectId(project.getId())).extracting(ProjectMember::getId).containsExactly(member.getId());
        assertThat(members.findActiveWithoutApplicationByMemberIdAndGisuIdAndMatchingType(
            10L, 1L, MatchingType.PLAN_DEVELOPER)).get().satisfies(published -> {
                assertThat(published.getId()).isEqualTo(member.getId());
                assertThat(published.getApplication()).isNull();
            });
        assertThat(members.listMatchedRoundsByProjectIdsAndMemberIds(Set.of(project.getId()), Set.of(10L)))
            .extracting(row -> row.matchingRoundId()).containsExactly(round.getId());
        assertThat(members.listAssignedMemberIdsByGisuId(1L)).containsExactly(10L);
    }

    @Test
    void 선발된_팀원이_없어도_미결정_지원서를_불합격으로_확정하고_공개한다() {
        // given
        Project project = project(100L);
        ProjectMatchingRound round = round(Instant.now().minusSeconds(600), true);
        ProjectApplication unselected = application(project, round, 10L, ProjectApplicationStatus.SUBMITTED);
        em.flush();
        em.clear();

        // when
        publisher.publish(round.getId());
        em.flush();
        em.clear();

        // then
        assertThat(em.find(ProjectApplication.class, unselected.getId()).getStatus()).isEqualTo(ProjectApplicationStatus.REJECTED);
        assertThat(em.find(ProjectMatchingRound.class, round.getId()).getResultPublishedAt()).isNotNull();
        assertThat(members.listByProjectId(project.getId())).isEmpty();
    }

    @Test
    void 공개_대기는_실제_배정으로_계산하지만_공개_조회에는_포함하지_않는다() {
        // given
        Project project = project(100L);
        ProjectMatchingRound round = round(Instant.now().plusSeconds(600), true);
        ProjectMember member = matchedMember(project, round, 10L);
        em.persist(ProjectPartQuota.create(project, ChallengerPart.WEB, 1L, 999L));
        em.flush();
        em.clear();

        // when - 발표 전 실행해도 공개하지 않음
        publisher.publish(round.getId());

        // then
        assertThat(members.existsByGisuAndMember(1L, 10L)).isTrue();
        assertThat(members.countAssignedByProjectIdGroupByPart(project.getId())).containsEntry(ChallengerPart.WEB, 1L);
        assertPublicViews(project, 0, PartQuotaStatus.RECRUITING);
        assertThat(em.find(ProjectMember.class, member.getId()).getStatus()).isEqualTo(ProjectMemberStatus.PENDING_PUBLICATION);
    }

    @Test
    void 공개_후_팀원과_집계와_모집_상태가_함께_반영된다() {
        // given
        Project project = project(100L);
        ProjectMatchingRound round = round(Instant.now().minusSeconds(600), true);
        matchedMember(project, round, 10L);
        em.persist(ProjectPartQuota.create(project, ChallengerPart.WEB, 1L, 999L));
        em.flush();
        em.clear();
        assertPublicViews(project, 0, PartQuotaStatus.RECRUITING);
        assertThat(rounds.listAllNotPublished()).extracting(ProjectMatchingRound::getId).containsExactly(round.getId());

        // when
        publisher.publish(round.getId());
        em.flush();
        em.clear();

        // then
        assertPublicViews(project, 1, PartQuotaStatus.COMPLETED);
        assertThat(members.listMatchedRoundsByProjectIdsAndMemberIds(Set.of(project.getId()), Set.of(10L)))
            .extracting(row -> row.matchingRoundId()).containsExactly(round.getId());
        assertThat(rounds.listAllNotPublished()).isEmpty();
    }

    @Test
    void 자동_선발이_완료되지_않은_차수는_공개하지_않는다() {
        // given
        Project project = project(100L);
        ProjectMatchingRound round = round(Instant.now().minusSeconds(600), false);
        ProjectMember member = matchedMember(project, round, 10L);
        em.flush();
        em.clear();

        // when
        assertThat(publisher.publish(round.getId())).isFalse();

        // then
        assertThat(members.listByProjectId(project.getId())).isEmpty();
        assertThat(em.find(ProjectMember.class, member.getId()).getStatus()).isEqualTo(ProjectMemberStatus.PENDING_PUBLICATION);
        assertThat(em.find(ProjectMatchingRound.class, round.getId()).getResultPublishedAt()).isNull();
    }

    @Test
    void 공개_전에_해제되거나_프로젝트가_중단된_배정은_활성화하지_않는다() {
        // given
        Project project = project(100L);
        ProjectMatchingRound round = round(Instant.now().minusSeconds(600), true);
        ProjectMember dismissed = matchedMember(project, round, 10L);
        dismissed.dismiss("배정 해제", 999L);
        ProjectMember pending = matchedMember(project, round, 11L);
        project.abort("프로젝트 중단", 999L);
        em.flush();
        em.clear();

        // when
        publisher.publish(round.getId());
        em.flush();
        em.clear();

        // then
        assertThat(members.listByProjectId(project.getId())).isEmpty();
        assertThat(em.find(ProjectMember.class, dismissed.getId()).getStatus()).isEqualTo(ProjectMemberStatus.DISMISSED);
        assertThat(em.find(ProjectMember.class, pending.getId()).getStatus()).isEqualTo(ProjectMemberStatus.WITHDRAWN);
    }

    private void assertPublicViews(Project project, int expectedCount, PartQuotaStatus status) {
        assertThat(members.listByProjectId(project.getId())).hasSize(expectedCount);
        assertThat(members.listByProjectIds(Set.of(project.getId())).getOrDefault(project.getId(), List.of()))
            .hasSize(expectedCount);
        assertThat(members.countByProjectIdGroupByPart(project.getId()).getOrDefault(ChallengerPart.WEB, 0L))
            .isEqualTo((long)expectedCount);
        assertThat(statistics.listPublicActiveMembersByChapterId(1L)).hasSize(expectedCount);
        assertThat(statistics.listApprovedApplicationsByProjectIds(Set.of(project.getId()))).hasSize(expectedCount);
        for (List<ChallengerPart> parts : List.of(List.of(ChallengerPart.WEB), List.<ChallengerPart>of())) {
            var page = projects.search(SearchProjectQuery.forChallenger(
                1L, null, null, null, parts, status, PageRequest.of(0, 20)));
            assertThat(page.getContent()).extracting(Project::getId).containsExactly(project.getId());
            assertThat(page.getTotalElements()).isEqualTo(1);
        }
    }

    private Project project(Long ownerId) {
        Project project = Project.createDraft(1L, 1L, ownerId, 1L, ownerId);
        ReflectionTestUtils.setField(project, "status", ProjectStatus.IN_PROGRESS);
        ReflectionTestUtils.setField(project, "name", "공개 테스트");
        return em.persist(project);
    }

    private ProjectMatchingRound round(Instant publishAt, boolean autoDecided) {
        ProjectMatchingRound round = ProjectMatchingRound.create("개발 1차", null, MatchingType.PLAN_DEVELOPER,
            MatchingPhase.FIRST, 1L, publishAt.minusSeconds(14400), publishAt.minusSeconds(7200),
            publishAt.minusSeconds(3600), publishAt);
        if (autoDecided) {
            round.executeAutoDecision(null);
        }
        return em.persist(round);
    }

    private ProjectMember matchedMember(Project project, ProjectMatchingRound round, Long memberId) {
        ProjectApplication app = application(project, round, memberId, ProjectApplicationStatus.APPROVED);
        return em.persist(ProjectMember.createFromApplication(app, ChallengerPart.WEB, 999L));
    }

    private ProjectApplication application(Project project, ProjectMatchingRound round, Long memberId,
        ProjectApplicationStatus status) {
        ProjectApplicationForm form = forms.computeIfAbsent(project.getId(),
            ignored -> em.persist(ProjectApplicationForm.create(project, 500L)));
        ProjectApplication app = ProjectApplication.create(form, memberId * 10, memberId, round);
        ReflectionTestUtils.setField(app, "status", status);
        return em.persist(app);
    }
}
