package com.umc.product.project.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectMemberView;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.service.policy.ProjectManualMatchingAccessPolicy;
import com.umc.product.project.application.service.policy.ProjectMatchingAdjustmentPolicy;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.PartQuotaStatus;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

@ExtendWith(MockitoExtension.class)
class ProjectMatchingOverviewQueryServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-27T03:00:00Z");
    private static final Long CHAPTER_ID = 10L;
    private static final Long GISU_ID = 3L;
    private static final Long REQUESTER_ID = 99L;

    @Mock
    LoadProjectPort loadProjectPort;

    @Mock
    LoadProjectMemberPort loadProjectMemberPort;

    @Mock
    LoadProjectPartQuotaPort loadProjectPartQuotaPort;

    @Mock
    LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;

    @Mock
    GetChapterUseCase getChapterUseCase;

    @Mock
    GetMemberUseCase getMemberUseCase;

    @Mock
    Clock clock;

    @Mock
    ProjectManualMatchingAccessPolicy manualMatchingAccessPolicy;

    @Mock
    ProjectMatchingAdjustmentPolicy matchingAdjustmentPolicy;

    @InjectMocks
    ProjectMatchingOverviewQueryService sut;

    @BeforeEach
    void setUp() {
        lenient().when(clock.instant()).thenReturn(NOW);
        given(getChapterUseCase.getGisuIdByChapterId(CHAPTER_ID)).willReturn(GISU_ID);
    }

    @Test
    void 매칭_현황_표의_인원과_정원_집계() {
        // given
        Project project = project(100L, CHAPTER_ID);
        given(loadProjectPort.listPublicByChapterId(CHAPTER_ID))
            .willReturn(List.of(project(100L, CHAPTER_ID)));
        given(loadProjectPartQuotaPort.listByProjectIdsGroupedByProjectId(Set.of(100L)))
            .willReturn(Map.of(100L, List.of(
                ProjectPartQuota.create(project, ChallengerPart.DESIGN, 1L, REQUESTER_ID),
                ProjectPartQuota.create(project, ChallengerPart.WEB, 2L, REQUESTER_ID),
                ProjectPartQuota.create(project, ChallengerPart.PLAN, 2L, REQUESTER_ID))));
        ProjectMember developer = ProjectMember.create(project, 1L, ChallengerPart.WEB, REQUESTER_ID);
        given(loadProjectMemberPort.listMatchingOverviewMembersByProjectIds(Set.of(100L), List.of()))
            .willReturn(List.of(
                developer,
                ProjectMember.create(project, REQUESTER_ID, ChallengerPart.PLAN, REQUESTER_ID),
                ProjectMember.create(project, 2L, ChallengerPart.PLAN, REQUESTER_ID)));

        // when
        var result = sut.getByChapterId(CHAPTER_ID, ProjectMemberView.PUBLISHED, REQUESTER_ID);

        // then
        var item = result.projects().getFirst();
        assertThat(item.currentMemberCount()).isEqualTo(1);
        assertThat(item.quota()).isEqualTo(3);
        assertThat(item.partQuotaStatus()).isEqualTo(PartQuotaStatus.RECRUITING);
        assertThat(item.productOwner().memberId()).isEqualTo(REQUESTER_ID);
        assertThat(item.coProductOwners()).extracting(person -> person.memberId()).containsExactly(2L);
        assertThat(item.parts()).filteredOn(part -> part.part() == ChallengerPart.DESIGN)
            .allSatisfy(part -> {
                assertThat(part.currentMemberCount()).isZero();
                assertThat(part.quota()).isEqualTo(1);
                assertThat(part.status()).isEqualTo(PartQuotaStatus.RECRUITING);
                assertThat(part.members()).isEmpty();
            });
        var web = item.parts().stream()
            .filter(part -> part.part() == ChallengerPart.WEB)
            .findFirst()
            .orElseThrow();
        assertThat(web.members()).hasSize(1);
        assertThat(web.status()).isEqualTo(PartQuotaStatus.RECRUITING);
        assertThat(web.members().getFirst().matchedRoundInfo()).isNull();
        verify(loadProjectMemberPort).listMatchingOverviewMembersByProjectIds(Set.of(100L), List.of());
    }

    @Test
    void 조정_현황은_적용_차수로_조회() {
        // given
        Project project = project(100L, CHAPTER_ID);
        ProjectMatchingRound random = randomRound();
        given(loadProjectPort.listPublicByChapterId(CHAPTER_ID))
            .willReturn(List.of(project(100L, CHAPTER_ID)));
        given(loadProjectMatchingRoundPort.listByChapterId(CHAPTER_ID)).willReturn(List.of(random));
        given(matchingAdjustmentPolicy.findViewableAdjustmentRounds(List.of(random), NOW)).willReturn(List.of(random));
        given(loadProjectMemberPort.listMatchingOverviewMembersByProjectIds(Set.of(100L), List.of(24L)))
            .willReturn(List.of(ProjectMember.createForMatching(
                project, 1L, ChallengerPart.WEB, random, REQUESTER_ID)));

        // when
        var result = sut.getByChapterId(CHAPTER_ID, ProjectMemberView.ADJUSTMENT, REQUESTER_ID);

        // then
        verify(manualMatchingAccessPolicy).validateChapter(REQUESTER_ID, GISU_ID, CHAPTER_ID);
        assertThat(result.projects().getFirst().currentMemberCount()).isEqualTo(1);
        var member = result.projects().getFirst().parts().getFirst().members().getFirst();
        assertThat(member.matchedRoundInfo().matchingRoundId()).isEqualTo(24L);
        assertThat(member.matchedRoundInfo().phase()).isEqualTo(MatchingPhase.RANDOM_MATCHING);
    }

    @Test
    void 빈_지부의_조정_권한_검증() {
        // given
        doThrow(new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED))
            .when(manualMatchingAccessPolicy).validateChapter(REQUESTER_ID, GISU_ID, CHAPTER_ID);

        // when & then
        assertThatThrownBy(() -> sut.getByChapterId(
            CHAPTER_ID, ProjectMemberView.ADJUSTMENT, REQUESTER_ID)).isInstanceOf(ProjectDomainException.class);
        verify(loadProjectPort, never()).listPublicByChapterId(CHAPTER_ID);
    }

    @Test
    void 매칭_현황은_모집상태와_관계없이_조회순서_유지() {
        // given
        Project first = project(100L, CHAPTER_ID);
        Project second = project(101L, CHAPTER_ID);
        given(loadProjectPort.listPublicByChapterId(CHAPTER_ID))
            .willReturn(List.of(project(100L, CHAPTER_ID), project(101L, CHAPTER_ID)));
        given(loadProjectPartQuotaPort.listByProjectIdsGroupedByProjectId(Set.of(100L, 101L))).willReturn(Map.of(
            100L, List.of(ProjectPartQuota.create(first, ChallengerPart.WEB, 1L, REQUESTER_ID)),
            101L, List.of(ProjectPartQuota.create(second, ChallengerPart.WEB, 1L, REQUESTER_ID))));
        given(loadProjectMemberPort.listMatchingOverviewMembersByProjectIds(Set.of(100L, 101L), List.of()))
            .willReturn(List.of(ProjectMember.create(first, 1L, ChallengerPart.WEB, REQUESTER_ID)));

        // when
        var result = sut.getByChapterId(CHAPTER_ID, ProjectMemberView.PUBLISHED, REQUESTER_ID);

        // then
        assertThat(result.projects()).extracting(item -> item.projectId()).containsExactly(100L, 101L);
        assertThat(result.projects().getFirst().parts().getFirst().status()).isEqualTo(PartQuotaStatus.COMPLETED);
    }

    @Test
    void 조정_조회_시점_외에는_공개_명단_반환() {
        // given
        given(loadProjectPort.listPublicByChapterId(CHAPTER_ID))
            .willReturn(List.of(project(100L, CHAPTER_ID)));

        // when
        var result = sut.getByChapterId(CHAPTER_ID, ProjectMemberView.ADJUSTMENT, REQUESTER_ID);

        // then
        verify(manualMatchingAccessPolicy).validateChapter(REQUESTER_ID, GISU_ID, CHAPTER_ID);
        verify(loadProjectMemberPort).listMatchingOverviewMembersByProjectIds(Set.of(100L), List.of());
        assertThat(result.projects().getFirst().currentMemberCount()).isZero();
    }

    @Test
    void 매칭_현황_표의_빈_목록() {
        // when
        var result = sut.getByChapterId(CHAPTER_ID, ProjectMemberView.PUBLISHED, REQUESTER_ID);

        // then
        assertThat(result.projects()).isEmpty();
        verify(loadProjectMemberPort, never()).listMatchingOverviewMembersByProjectIds(any(), any());
    }

    private static Project project(Long id, Long chapterId) {
        Project project = Project.createDraft(GISU_ID, chapterId, REQUESTER_ID, 501L, REQUESTER_ID);
        ReflectionTestUtils.setField(project, "id", id);
        ReflectionTestUtils.setField(project, "name", "프로젝트 " + id);
        ReflectionTestUtils.setField(project, "status", ProjectStatus.IN_PROGRESS);
        return project;
    }

    private static ProjectMatchingRound randomRound() {
        Instant endsAt = NOW.plusSeconds(60);
        ProjectMatchingRound round = ProjectMatchingRound.create("랜덤", null, MatchingType.PLAN_DEVELOPER,
            MatchingPhase.RANDOM_MATCHING, CHAPTER_ID,
            NOW.minusSeconds(60), endsAt, endsAt, endsAt.plus(5, ChronoUnit.MINUTES));
        ReflectionTestUtils.setField(round, "id", 24L);
        return round;
    }
}
