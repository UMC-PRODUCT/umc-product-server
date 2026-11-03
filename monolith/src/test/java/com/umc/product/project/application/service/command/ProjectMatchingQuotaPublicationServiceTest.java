package com.umc.product.project.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.SaveProjectPartQuotaPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectStatus;

@ExtendWith(MockitoExtension.class)
class ProjectMatchingQuotaPublicationServiceTest {
    @Mock LoadProjectPort projects;
    @Mock LoadProjectMemberPort members;
    @Mock LoadProjectPartQuotaPort quotas;
    @Mock SaveProjectPartQuotaPort saveQuotas;
    @InjectMocks ProjectMatchingQuotaPublicationService sut;

    @ParameterizedTest
    @EnumSource(MatchingType.class)
    void 발표_유형의_초과_TO만_늘리고_PM과_다른_유형은_유지한다(MatchingType type) {
        // given
        Project project = project();
        var design = ProjectPartQuota.create(project, ChallengerPart.DESIGN, 1L, 10L);
        var web = ProjectPartQuota.create(project, ChallengerPart.WEB_PRODUCT_ENGINEER, 1L, 10L);
        var pm = ProjectPartQuota.create(project, ChallengerPart.PLAN, 1L, 10L);
        given(projects.listByChapterIdAndStatus(2L, ProjectStatus.IN_PROGRESS)).willReturn(List.of(project));
        given(quotas.listByProjectIdsGroupedByProjectId(List.of(1L)))
            .willReturn(Map.of(1L, List.of(design, web, pm)));
        given(members.countByProjectIdsGroupByProjectIdAndPart(List.of(1L)))
            .willReturn(Map.of(1L, Map.of(ChallengerPart.DESIGN, 3L,
                ChallengerPart.WEB_PRODUCT_ENGINEER, 4L, ChallengerPart.PLAN, 2L)));

        // when
        sut.synchronize(round(type));

        // then
        assertThat(design.getQuota()).isEqualTo(type == MatchingType.PLAN_DESIGN ? 3 : 1);
        assertThat(web.getQuota()).isEqualTo(type == MatchingType.PLAN_DEVELOPER ? 4 : 1);
        assertThat(pm.getQuota()).isEqualTo(1);
    }

    @Test
    void 확정인원이_기존_TO_이하이면_줄이지_않고_저장하지_않는다() {
        // given
        Project project = project();
        var design = ProjectPartQuota.create(project, ChallengerPart.DESIGN, 4L, 10L);
        var web = ProjectPartQuota.create(project, ChallengerPart.WEB_PRODUCT_ENGINEER, 5L, 10L);
        given(projects.listByChapterIdAndStatus(2L, ProjectStatus.IN_PROGRESS)).willReturn(List.of(project));
        given(quotas.listByProjectIdsGroupedByProjectId(List.of(1L))).willReturn(Map.of(1L, List.of(design, web)));
        given(members.countByProjectIdsGroupByProjectIdAndPart(List.of(1L)))
            .willReturn(Map.of(1L, Map.of(ChallengerPart.DESIGN, 4L, ChallengerPart.WEB_PRODUCT_ENGINEER, 3L)));

        // when
        sut.synchronize(round(MatchingType.PLAN_DESIGN));
        sut.synchronize(round(MatchingType.PLAN_DEVELOPER));

        // then
        assertThat(design.getQuota()).isEqualTo(4);
        assertThat(web.getQuota()).isEqualTo(5);
        assertThat(web.getLastEditedMemberId()).isEqualTo(10);
        then(saveQuotas).should(never()).saveAll(any());
    }

    @Test
    void TO가_없던_파트도_확정_인원으로_생성하고_레거시_개발파트를_포함한다() {
        // given
        Project project = project();
        given(projects.listByChapterIdAndStatus(2L, ProjectStatus.IN_PROGRESS)).willReturn(List.of(project));
        given(members.countByProjectIdsGroupByProjectIdAndPart(List.of(1L)))
            .willReturn(Map.of(1L, Map.of(ChallengerPart.SPRINGBOOT, 3L)));

        // when
        sut.synchronize(round(MatchingType.PLAN_DEVELOPER));

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ProjectPartQuota>> captor = ArgumentCaptor.forClass(List.class);
        then(saveQuotas).should().saveAll(captor.capture());
        assertThat(captor.getValue()).singleElement().satisfies(quota -> {
            assertThat(quota.getProject().getId()).isEqualTo(1L);
            assertThat(quota.getPart()).isEqualTo(ChallengerPart.SPRINGBOOT);
            assertThat(quota.getQuota()).isEqualTo(3);
        });
    }

    private Project project() {
        Project project = Project.createDraft(1L, 2L, 3L, 4L, 3L);
        ReflectionTestUtils.setField(project, "id", 1L);
        return project;
    }

    private ProjectMatchingRound round(MatchingType type) {
        Instant now = Instant.now();
        return ProjectMatchingRound.create("랜덤", null, type, MatchingPhase.RANDOM_MATCHING, 2L,
            now.minusSeconds(7200), now.minusSeconds(3600), now.minusSeconds(3600), now.minusSeconds(600));
    }
}
