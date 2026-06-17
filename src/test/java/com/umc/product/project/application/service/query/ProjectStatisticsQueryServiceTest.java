package com.umc.product.project.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.project.application.port.in.query.dto.statistics.ChapterProjectStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectStatisticsInfo;
import com.umc.product.project.application.port.out.LoadProjectStatisticsPort;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsApplicationRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsMatchingRoundRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsMemberRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsProjectRow;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.enums.ProjectMemberStatus;

@ExtendWith(MockitoExtension.class)
class ProjectStatisticsQueryServiceTest {

    @Mock
    LoadProjectStatisticsPort loadProjectStatisticsPort;

    @Mock
    GetChallengerUseCase getChallengerUseCase;

    @Mock
    GetMemberUseCase getMemberUseCase;

    @InjectMocks
    ProjectStatisticsQueryService sut;

    @Test
    @DisplayName("getByProjectId_기존_멤버_지원_이력과_차수별_BFF_통계를_함께_반환한다")
    void 단건_프로젝트_BFF_통계_반환() {
        // given
        Long projectId = 10L;
        Long gisuId = 1L;
        Long chapterId = 3L;

        given(loadProjectStatisticsPort.getProjectById(projectId))
            .willReturn(projectRow(projectId, gisuId, chapterId));
        given(loadProjectStatisticsPort.listMatchingRoundsByChapterId(chapterId))
            .willReturn(List.of(
                roundRow(1L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST),
                roundRow(2L, MatchingType.PLAN_DEVELOPER, MatchingPhase.SECOND)
            ));
        given(loadProjectStatisticsPort.listActiveMembersByProjectId(projectId))
            .willReturn(List.of(
                memberRow(projectId, 101L, 1001L, ChallengerPart.WEB),
                memberRow(projectId, 102L, 1002L, ChallengerPart.DESIGN)
            ));
        given(loadProjectStatisticsPort.listCountedApplicationsByProjectIds(Set.of(projectId)))
            .willReturn(List.of(
                applicationRow(projectId, 1001L, 201L, ProjectApplicationStatus.APPROVED, 1L,
                    MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST),
                applicationRow(projectId, 1003L, 202L, ProjectApplicationStatus.REJECTED, 1L,
                    MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST),
                applicationRow(projectId, 1004L, 203L, ProjectApplicationStatus.SUBMITTED, 2L,
                    MatchingType.PLAN_DEVELOPER, MatchingPhase.SECOND)
            ));
        given(getChallengerUseCase.listByChapterId(chapterId))
            .willReturn(List.of(
                challenger(1001L, gisuId, ChallengerPart.WEB),
                challenger(1002L, gisuId, ChallengerPart.DESIGN),
                challenger(1003L, gisuId, ChallengerPart.WEB),
                challenger(1004L, gisuId, ChallengerPart.ANDROID),
                challenger(9001L, gisuId, ChallengerPart.PLAN),
                challenger(9002L, gisuId, ChallengerPart.ADMIN),
                // 수료/제명 챌린저는 지부 모집단(지원 가능 인원)에서 제외되어야 한다.
                challenger(1005L, gisuId, ChallengerPart.WEB, ChallengerStatus.GRADUATED),
                challenger(1006L, gisuId, ChallengerPart.SPRINGBOOT, ChallengerStatus.EXPELLED)
            ));
        given(getMemberUseCase.findAllSchoolIdsByIds(Set.of(1001L, 1002L, 1003L, 1004L)))
            .willReturn(Map.of(
                1001L, 501L,
                1002L, 502L,
                1003L, 502L,
                1004L, 501L
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
                tuple(201L, ProjectApplicationStatus.APPROVED)
            );
        assertThat(result.projectMembers().get(0).applications())
            .extracting(a -> a.matchingRound().type(), a -> a.matchingRound().phase())
            .containsExactly(
                tuple(MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST)
            );
        assertThat(result.projectMembers().get(1).applications()).isEmpty();

        assertThat(result.roundApplicationStatistics())
            .extracting(
                s -> s.matchingRound().matchingRoundId(),
                s -> s.appliedMemberCount(),
                s -> s.availableMemberCount()
            )
            .containsExactly(
                tuple(1L, 2L, 4L),
                tuple(2L, 1L, 3L)
            );
        assertThat(result.schoolApplicationStatistics().get(0).matchingRound().matchingRoundId()).isEqualTo(1L);
        assertThat(result.schoolApplicationStatistics().get(0).schools())
            .extracting("schoolId", "applicantCount")
            .containsExactly(
                tuple(501L, 1L),
                tuple(502L, 1L)
            );
        assertThat(result.schoolApplicationStatistics().get(1).matchingRound().matchingRoundId()).isEqualTo(2L);
        assertThat(result.schoolApplicationStatistics().get(1).schools())
            .extracting("schoolId", "applicantCount")
            .containsExactly(tuple(501L, 1L));
    }

    @Test
    @DisplayName("getByChapterId_프로젝트별_기존_필드와_지부_BFF_요약을_함께_반환한다")
    void 지부_BFF_통계_반환() {
        // given
        Long chapterId = 3L;
        Long gisuId = 1L;
        given(loadProjectStatisticsPort.listProjectsByChapterId(chapterId))
            .willReturn(List.of(
                projectRow(10L, gisuId, chapterId),
                projectRow(11L, gisuId, chapterId)
            ));
        given(loadProjectStatisticsPort.listMatchingRoundsByChapterId(chapterId))
            .willReturn(List.of(
                roundRow(1L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST),
                roundRow(2L, MatchingType.PLAN_DEVELOPER, MatchingPhase.SECOND)
            ));
        given(loadProjectStatisticsPort.listActiveMembersByChapterId(chapterId))
            .willReturn(List.of(
                memberRow(10L, 101L, 1001L, ChallengerPart.WEB),
                memberRow(10L, 102L, 1002L, ChallengerPart.DESIGN),
                memberRow(11L, 201L, 1004L, ChallengerPart.WEB)
            ));
        given(loadProjectStatisticsPort.listCountedApplicationsByProjectIds(Set.of(10L, 11L)))
            .willReturn(List.of(
                applicationRow(10L, 1001L, 301L, ProjectApplicationStatus.APPROVED, 1L,
                    MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST),
                applicationRow(10L, 1003L, 302L, ProjectApplicationStatus.REJECTED, 1L,
                    MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST),
                applicationRow(11L, 1003L, 303L, ProjectApplicationStatus.SUBMITTED, 1L,
                    MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST),
                applicationRow(11L, 1004L, 304L, ProjectApplicationStatus.APPROVED, 2L,
                    MatchingType.PLAN_DEVELOPER, MatchingPhase.SECOND),
                applicationRow(10L, 9001L, 305L, ProjectApplicationStatus.SUBMITTED, 2L,
                    MatchingType.PLAN_DEVELOPER, MatchingPhase.SECOND)
            ));
        given(getChallengerUseCase.listByChapterId(chapterId))
            .willReturn(List.of(
                challenger(1001L, gisuId, ChallengerPart.WEB),
                challenger(1002L, gisuId, ChallengerPart.DESIGN),
                challenger(1003L, gisuId, ChallengerPart.WEB),
                challenger(1004L, gisuId, ChallengerPart.ANDROID),
                challenger(9001L, gisuId, ChallengerPart.PLAN),
                challenger(9002L, gisuId, ChallengerPart.ADMIN)
            ));
        given(getMemberUseCase.findAllSchoolIdsByIds(Set.of(1001L, 1002L, 1003L, 1004L)))
            .willReturn(Map.of(
                1001L, 501L,
                1002L, 502L,
                1003L, 502L,
                1004L, 501L
            ));

        // when
        ChapterProjectStatisticsInfo result = sut.getByChapterId(chapterId);

        // then
        assertThat(result.chapterId()).isEqualTo(chapterId);
        assertThat(result.projects())
            .extracting(ProjectStatisticsInfo::projectId)
            .containsExactly(10L, 11L);
        assertThat(result.projects().get(0).projectMembers()).hasSize(2);
        assertThat(result.projects().get(0).projectMembers().get(0).applications())
            .extracting("applicationId", "status")
            .containsExactly(tuple(301L, ProjectApplicationStatus.APPROVED));
        assertThat(result.projects().get(0).projectMembers().get(1).applications()).isEmpty();
        assertThat(result.projects().get(1).projectMembers()).hasSize(1);
        assertThat(result.projects().get(1).projectMembers().get(0).applications())
            .extracting("applicationId", "status")
            .containsExactly(tuple(304L, ProjectApplicationStatus.APPROVED));

        assertThat(result.summary().roundApplicationStatistics())
            .extracting(
                s -> s.matchingRound().matchingRoundId(),
                s -> s.appliedMemberCount(),
                s -> s.availableMemberCount()
            )
            .containsExactly(
                tuple(1L, 2L, 4L),
                tuple(2L, 1L, 3L)
            );
        assertThat(result.summary().roundSchoolRankings().get(0).schools())
            .extracting("schoolId", "applicantCount")
            .containsExactly(
                tuple(501L, 1L),
                tuple(502L, 1L)
            );
        assertThat(result.summary().schoolMatchingStatistics())
            .extracting("schoolId", "matchedMemberCount", "totalMemberCount")
            .containsExactly(
                tuple(501L, 2L, 2L),
                tuple(502L, 0L, 2L)
            );
        assertThat(result.summary().projectRoundStatistics())
            .extracting("projectId")
            .containsExactly(10L, 11L);
        assertThat(result.summary().projectRoundStatistics().get(0).matchingRounds())
            .extracting(
                s -> s.matchingRound().matchingRoundId(),
                s -> s.appliedMemberCount(),
                s -> s.matchedMemberCount()
            )
            .containsExactly(
                tuple(1L, 2L, 1L),
                tuple(2L, 0L, 0L)
            );
        assertThat(result.summary().projectRoundStatistics().get(1).matchingRounds())
            .extracting(
                s -> s.matchingRound().matchingRoundId(),
                s -> s.appliedMemberCount(),
                s -> s.matchedMemberCount()
            )
            .containsExactly(
                tuple(1L, 1L, 0L),
                tuple(2L, 1L, 1L)
            );

        verify(loadProjectStatisticsPort).listProjectsByChapterId(chapterId);
        verify(loadProjectStatisticsPort).listActiveMembersByChapterId(chapterId);
    }

    private static ProjectStatisticsProjectRow projectRow(Long projectId, Long gisuId, Long chapterId) {
        return new ProjectStatisticsProjectRow(projectId, gisuId, chapterId);
    }

    private static ProjectStatisticsMatchingRoundRow roundRow(
        Long matchingRoundId,
        MatchingType matchingRoundType,
        MatchingPhase matchingRoundPhase
    ) {
        return new ProjectStatisticsMatchingRoundRow(matchingRoundId, matchingRoundType, matchingRoundPhase);
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

    private static ChallengerInfo challenger(Long memberId, Long gisuId, ChallengerPart part) {
        return challenger(memberId, gisuId, part, ChallengerStatus.ACTIVE);
    }

    private static ChallengerInfo challenger(
        Long memberId, Long gisuId, ChallengerPart part, ChallengerStatus status
    ) {
        return ChallengerInfo.builder()
            .memberId(memberId)
            .gisuId(gisuId)
            .part(part)
            .challengerStatus(status)
            .build();
    }
}
