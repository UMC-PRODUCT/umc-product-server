package com.umc.product.project.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectStatisticsInfo;
import com.umc.product.project.application.port.out.LoadProjectStatisticsPort;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsApplicationRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsMemberRow;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.enums.ProjectMemberStatus;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectStatisticsQueryServiceTest {

    @Mock
    LoadProjectStatisticsPort loadProjectStatisticsPort;

    @InjectMocks
    ProjectStatisticsQueryService sut;

    @Test
    @DisplayName("getByProjectId_프로젝트_멤버의_여러_차수_지원_이력을_모두_반환한다")
    void 단건_프로젝트_여러_차수_지원_이력_반환() {
        // given
        Long projectId = 10L;
        given(loadProjectStatisticsPort.listActiveMembersByProjectId(projectId))
            .willReturn(List.of(
                memberRow(projectId, 101L, 1001L, ChallengerPart.WEB),
                memberRow(projectId, 102L, 1002L, ChallengerPart.DESIGN)
            ));
        given(loadProjectStatisticsPort.listCountedApplicationsByProjectIdsAndMemberIds(
            Set.of(projectId), Set.of(1001L, 1002L)))
            .willReturn(List.of(
                applicationRow(projectId, 1001L, 201L, ProjectApplicationStatus.SUBMITTED, 1L,
                    MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST),
                applicationRow(projectId, 1001L, 202L, ProjectApplicationStatus.REJECTED, 2L,
                    MatchingType.PLAN_DEVELOPER, MatchingPhase.SECOND)
            ));

        // when
        ProjectStatisticsInfo result = sut.getByProjectId(projectId);

        // then
        assertThat(result.projectId()).isEqualTo(projectId);
        assertThat(result.projectMembers())
            .extracting("projectMemberId", "memberId", "part", "status")
            .containsExactly(
                tuple(101L, 1001L, ChallengerPart.WEB, ProjectMemberStatus.ACTIVE),
                tuple(102L, 1002L, ChallengerPart.DESIGN, ProjectMemberStatus.ACTIVE)
            );

        assertThat(result.projectMembers().get(0).applications())
            .extracting("applicationId", "status")
            .containsExactly(
                tuple(201L, ProjectApplicationStatus.SUBMITTED),
                tuple(202L, ProjectApplicationStatus.REJECTED)
            );
        assertThat(result.projectMembers().get(0).applications())
            .extracting(a -> a.matchingRound().type(), a -> a.matchingRound().phase())
            .containsExactly(
                tuple(MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST),
                tuple(MatchingType.PLAN_DEVELOPER, MatchingPhase.SECOND)
            );
        assertThat(result.projectMembers().get(1).applications()).isEmpty();
    }

    @Test
    @DisplayName("listByChapterId_지부_내_프로젝트별로_멤버와_지원_이력을_그룹핑한다")
    void 지부_프로젝트별_그룹핑() {
        // given
        Long chapterId = 3L;
        given(loadProjectStatisticsPort.listActiveMembersByChapterId(chapterId))
            .willReturn(List.of(
                memberRow(10L, 101L, 1001L, ChallengerPart.WEB),
                memberRow(11L, 201L, 2001L, ChallengerPart.DESIGN)
            ));
        given(loadProjectStatisticsPort.listCountedApplicationsByProjectIdsAndMemberIds(
            Set.of(10L, 11L), Set.of(1001L, 2001L)))
            .willReturn(List.of(
                applicationRow(11L, 2001L, 301L, ProjectApplicationStatus.APPROVED, 5L,
                    MatchingType.PLAN_DESIGN, MatchingPhase.FIRST),
                applicationRow(10L, 1001L, 302L, ProjectApplicationStatus.SUBMITTED, 6L,
                    MatchingType.PLAN_DEVELOPER, MatchingPhase.SECOND)
            ));

        // when
        List<ProjectStatisticsInfo> result = sut.listByChapterId(chapterId);

        // then
        assertThat(result)
            .extracting(ProjectStatisticsInfo::projectId)
            .containsExactly(10L, 11L);
        assertThat(result.get(0).projectMembers()).hasSize(1);
        assertThat(result.get(0).projectMembers().get(0).applications())
            .extracting("applicationId", "status")
            .containsExactly(tuple(302L, ProjectApplicationStatus.SUBMITTED));
        assertThat(result.get(1).projectMembers()).hasSize(1);
        assertThat(result.get(1).projectMembers().get(0).applications())
            .extracting("applicationId", "status")
            .containsExactly(tuple(301L, ProjectApplicationStatus.APPROVED));

        verify(loadProjectStatisticsPort).listActiveMembersByChapterId(chapterId);
    }

    private static ProjectStatisticsMemberRow memberRow(
        Long projectId, Long projectMemberId, Long memberId, ChallengerPart part
    ) {
        return new ProjectStatisticsMemberRow(projectId, projectMemberId, memberId, part, ProjectMemberStatus.ACTIVE);
    }

    private static ProjectStatisticsApplicationRow applicationRow(
        Long projectId,
        Long applicantMemberId,
        Long applicationId,
        ProjectApplicationStatus status,
        Long matchingRoundId,
        MatchingType matchingRoundType,
        MatchingPhase matchingRoundPhase
    ) {
        return new ProjectStatisticsApplicationRow(
            projectId,
            applicantMemberId,
            applicationId,
            status,
            matchingRoundId,
            matchingRoundType,
            matchingRoundPhase
        );
    }
}
