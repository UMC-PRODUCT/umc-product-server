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
    void 발표하면_PENDING_PUBLICATION_멤버의_지원서는_APPROVED로_나머지는_REJECTED로_변경한다() {
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
    void 지원서_없는_랜덤_매칭도_발표할_때_팀원과_랜덤_매칭_조회에_반영된다() {
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
    void 선발된_멤버가_없어도_SUBMITTED_지원서를_REJECTED로_변경하고_공개한다() {
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
    void PENDING_PUBLICATION은_정원에_포함하고_ACTIVE_조회에서는_제외한다() {
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
    void 발표할_때_DISMISSED_멤버는_유지하고_PENDING_PUBLICATION_멤버만_ACTIVE로_변경한다() {
        // given
        Project project = project(100L);
        ProjectMatchingRound round = round(Instant.now().minusSeconds(600), true);
        ProjectMember dismissed = matchedMember(project, round, 10L);
        dismissed.dismiss("DISMISSED 처리", 999L);
        ProjectMember pending = matchedMember(project, round, 11L);
        em.flush();
        em.clear();

        // when
        publisher.publish(round.getId());
        em.flush();
        em.clear();

        // then
        assertThat(members.listByProjectId(project.getId())).extracting(ProjectMember::getId)
            .containsExactly(pending.getId());
        assertThat(em.find(ProjectMember.class, dismissed.getId()).getStatus()).isEqualTo(ProjectMemberStatus.DISMISSED);
        assertThat(em.find(ProjectMember.class, pending.getId()).getStatus()).isEqualTo(ProjectMemberStatus.ACTIVE);
    }

    @Test
    void ABORTED_프로젝트의_멤버와_지원서는_발표해도_변경하지_않는다() {
        // given
        Project project = project(100L);
        ProjectMatchingRound round = round(Instant.now().minusSeconds(600), true);
        ProjectMember member = matchedMember(project, round, 10L);
        ReflectionTestUtils.setField(member.getApplication(), "statusChangedMemberId", 999L);
        ReflectionTestUtils.setField(member.getApplication(), "statusChangeReason", "PM 합격 결정");
        ReflectionTestUtils.setField(member.getApplication(), "statusChangedAt", Instant.now());
        ProjectApplication cancelled = application(project, round, 11L, ProjectApplicationStatus.SUBMITTED);
        cancelled.cancel(999L, "프로젝트 종료");
        ProjectMember dismissed = matchedMember(project, round, 12L);
        dismissed.dismiss("DISMISSED 처리", 999L);
        ProjectMember other = matchedMember(project(200L), round, 20L);
        project.abort("프로젝트 중단", 999L);
        member.withdraw("프로젝트 중단", 999L);
        em.flush();
        em.clear();
        Instant statusChangedAt = em.find(ProjectMember.class, member.getId()).getStatusUpdatedAt();
        Instant decidedAt = em.find(ProjectApplication.class, member.getApplication().getId()).getStatusChangedAt();

        // when
        assertThat(publisher.publish(round.getId())).isTrue();
        em.flush();
        em.clear();

        // then
        ProjectMember ended = em.find(ProjectMember.class, member.getId());
        assertThat(ended.getStatus()).isEqualTo(ProjectMemberStatus.WITHDRAWN);
        assertThat(ended.getStatusUpdatedAt()).isEqualTo(statusChangedAt);
        assertThat(ended.getStatusChangedMemberId()).isEqualTo(999L);
        ProjectApplication approved = em.find(ProjectApplication.class, member.getApplication().getId());
        assertThat(approved.getStatus()).isEqualTo(ProjectApplicationStatus.APPROVED);
        assertThat(approved.getStatusChangedAt()).isEqualTo(decidedAt);
        assertThat(approved.getStatusChangeReason()).isEqualTo("PM 합격 결정");
        assertThat(em.find(ProjectApplication.class, cancelled.getId()).getStatus()).isEqualTo(ProjectApplicationStatus.CANCELLED);
        assertThat(em.find(ProjectMember.class, dismissed.getId()).getStatus()).isEqualTo(ProjectMemberStatus.DISMISSED);
        assertThat(members.listActiveAndPendingPublicationByProjectId(project.getId())).isEmpty();
        assertThat(em.find(ProjectMember.class, other.getId()).getStatus()).isEqualTo(ProjectMemberStatus.ACTIVE);
    }

    @Test
    void 중단_대상은_ACTIVE와_PENDING_PUBLICATION이고_일반_조회는_ACTIVE만_반환한다() {
        // given
        Project project = project(100L);
        ProjectMatchingRound round = round(Instant.now().plusSeconds(600), true);
        ProjectMember active = em.persist(ProjectMember.create(project, 10L, ChallengerPart.WEB, 999L));
        ProjectMember pending = matchedMember(project, round, 11L);
        ProjectMember withdrawn = matchedMember(project, round, 12L);
        withdrawn.withdraw("탈퇴", 999L);
        ProjectMember completed = em.persist(ProjectMember.create(project, 13L, ChallengerPart.WEB, 999L));
        completed.complete(999L);
        ProjectMember dismissed = matchedMember(project, round, 14L);
        dismissed.dismiss("DISMISSED 처리", 999L);
        matchedMember(project(200L), round, 20L);
        em.flush();
        em.clear();

        // when
        List<ProjectMember> result = members.listActiveAndPendingPublicationByProjectId(project.getId());

        // then
        assertThat(result).extracting(ProjectMember::getId).containsExactly(active.getId(), pending.getId());
        assertThat(members.listByProjectId(project.getId())).extracting(ProjectMember::getId).containsExactly(active.getId());
        assertThat(members.existsByGisuAndMember(1L, 10L)).isTrue();
        assertThat(members.existsByGisuAndMember(1L, 11L)).isTrue();
        assertThat(members.existsByGisuAndMember(1L, 12L)).isFalse();
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
