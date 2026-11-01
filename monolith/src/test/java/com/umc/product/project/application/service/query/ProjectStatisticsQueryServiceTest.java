package com.umc.product.project.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterWithSchoolsInfo;
import com.umc.product.organization.application.port.in.query.dto.school.SchoolDetailInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectApplicationStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectApplicationStatisticsInfo.ApplicationPhaseCountInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectMatchingStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.RoundApplicationStatisticsInfo;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.LoadProjectStatisticsPort;
import com.umc.product.project.application.port.out.dto.ProjectMemberMatchedRoundInfo;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsApplicationRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsMatchedMemberRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsMemberRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsProjectRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsTargetProjectRow;
import com.umc.product.project.application.service.policy.ProjectMatchingChapterResolver;
import com.umc.product.project.application.service.policy.ProjectMatchingChapterResolver.ChapterTransferDelta;
import com.umc.product.project.application.service.policy.ProjectStatisticsAccessPolicy;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.enums.ProjectMemberStatus;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;

@ExtendWith(MockitoExtension.class)
class ProjectStatisticsQueryServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-27T03:00:00Z");
    private static final Long CHAPTER_ID = 10L;
    private static final Long GISU_ID = 3L;
    private static final Long REQUESTER_ID = 99L;

    @Mock
    LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;

    @Mock
    LoadProjectMemberPort loadProjectMemberPort;

    @Mock
    GetChapterUseCase getChapterUseCase;

    @Mock
    GetSchoolUseCase getSchoolUseCase;

    @Mock
    LoadProjectPartQuotaPort loadProjectPartQuotaPort;

    @Mock
    Clock clock;

    @Mock
    LoadProjectStatisticsPort loadProjectStatisticsPort;

    @Mock
    GetChallengerUseCase getChallengerUseCase;

    @Mock
    GetMemberUseCase getMemberUseCase;

    @Mock
    LoadProjectPort loadProjectPort;

    @Mock
    ProjectStatisticsAccessPolicy projectStatisticsAccessPolicy;

    @Mock
    ProjectMatchingChapterResolver matchingChapterResolver;

    @InjectMocks
    ProjectStatisticsQueryService sut;

    @BeforeEach
    void setUp() {
        lenient().when(matchingChapterResolver.deltaOf(any())).thenReturn(ChapterTransferDelta.empty());
        lenient().when(clock.instant()).thenReturn(NOW);
        lenient().when(projectStatisticsAccessPolicy.canReadChapterStatistics(REQUESTER_ID, CHAPTER_ID))
            .thenReturn(true);
        lenient().when(projectStatisticsAccessPolicy.canReadProjectStatistics(any(), any())).thenReturn(true);
        lenient().when(getChapterUseCase.getGisuIdByChapterId(CHAPTER_ID)).thenReturn(GISU_ID);
        lenient().when(loadProjectStatisticsPort.listProjectsByChapterId(CHAPTER_ID)).thenReturn(List.of(
            new ProjectStatisticsProjectRow(100L, GISU_ID, CHAPTER_ID),
            new ProjectStatisticsProjectRow(101L, GISU_ID, CHAPTER_ID)));
        lenient().when(loadProjectPort.listByIds(Set.of(100L, 101L)))
            .thenReturn(List.of(project(100L, CHAPTER_ID), project(101L, CHAPTER_ID)));
        lenient().when(loadProjectStatisticsPort.listApplicationStatisticsProjectsByIds(Set.of(100L, 101L)))
            .thenReturn(List.of(targetProject(100L), targetProject(101L)));
        lenient().when(loadProjectStatisticsPort.listApplicationStatisticsProjectsByIds(Set.of(100L)))
            .thenReturn(List.of(targetProject(100L)));
        lenient().when(loadProjectPort.listByIds(Set.of(100L))).thenReturn(List.of(project(100L, CHAPTER_ID)));
        lenient().when(loadProjectMatchingRoundPort.listByChapterId(CHAPTER_ID)).thenReturn(List.of(
            round(21L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST, -10, -9),
            round(22L, MatchingType.PLAN_DEVELOPER, MatchingPhase.SECOND, -1, 1),
            round(23L, MatchingType.PLAN_DEVELOPER, MatchingPhase.THIRD, 5, 6)));
        lenient().when(getChallengerUseCase.listByChapterId(CHAPTER_ID)).thenReturn(List.of(
            challenger(1L, ChallengerPart.WEB), challenger(2L, ChallengerPart.IOS),
            challenger(3L, ChallengerPart.WEB_PRODUCT_ENGINEER), challenger(4L, ChallengerPart.DESIGN),
            challenger(5L, ChallengerPart.PLAN), challenger(6L, ChallengerPart.ADMIN), challenger(9003L, null)));
        lenient().when(getMemberUseCase.findAllSchoolIdsByIds(Set.of(1L, 2L, 3L)))
            .thenReturn(Map.of(1L, 501L, 2L, 501L, 3L, 502L));
        lenient().when(getChapterUseCase.getChaptersWithSchoolsByGisuId(GISU_ID)).thenReturn(List.of(
            new ChapterWithSchoolsInfo(CHAPTER_ID, "지부", List.of(
                new ChapterWithSchoolsInfo.SchoolInfo(501L, "가톨릭대학교"),
                new ChapterWithSchoolsInfo.SchoolInfo(502L, "세종대학교"),
                new ChapterWithSchoolsInfo.SchoolInfo(503L, "지원 가능 인원이 없는 학교")))));
    }

    @Test
    @DisplayName("getByProjectId_기존_멤버_지원_이력과_차수별_BFF_통계를_함께_반환한다")
    void 단건_프로젝트_BFF_통계_반환() {
        // given
        Long projectId = 10L;
        Long gisuId = 1L;
        Long chapterId = 3L;

        given(loadProjectStatisticsPort.getProjectById(projectId))
            .willReturn(projectRow(projectId, gisuId, chapterId));
        given(loadProjectMatchingRoundPort.listByChapterId(chapterId))
            .willReturn(List.of(
                round(2L, MatchingType.PLAN_DEVELOPER, MatchingPhase.SECOND, -10, -9, chapterId),
                round(1L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST, -10, -9, chapterId)
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
                challenger(9003L, gisuId, null),
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
        // 요청자가 해당 프로젝트의 PO → FULL (멤버 단위 포함)
        Long requesterMemberId = 7000L;
        given(loadProjectPort.getById(projectId))
            .willReturn(project(projectId, requesterMemberId, chapterId));
        given(projectStatisticsAccessPolicy.canReadProjectStatistics(eq(requesterMemberId), any(Project.class)))
            .willReturn(true);

        // when
        ProjectStatisticsInfo result = sut.getByProjectId(projectId, requesterMemberId);

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
    @DisplayName("개발 매칭 통계는 디자인을 제외하고 개발·랜덤 인원과 개발 파트 TO를 집계한다")
    void 공개_프로젝트_매칭_요약_반환() {
        // given
        given(loadProjectStatisticsPort.listPublicProjectsByChapterId(CHAPTER_ID))
            .willReturn(List.of(targetProject(100L), targetProject(101L), targetProject(102L)));
        given(getChallengerUseCase.listByChapterId(CHAPTER_ID)).willReturn(List.of(
            challenger(1L, ChallengerPart.WEB), challenger(2L, ChallengerPart.IOS),
            challenger(3L, ChallengerPart.WEB), challenger(4L, ChallengerPart.DESIGN),
            challenger(5L, ChallengerPart.WEB), challenger(6L, ChallengerPart.PLAN),
            challenger(7L, ChallengerPart.ADMIN), challenger(8L, 99L, ChallengerPart.WEB),
            challenger(9L, GISU_ID, ChallengerPart.WEB, ChallengerStatus.WITHDRAWN), challenger(10L, null)));
        given(getMemberUseCase.findAllSchoolIdsByIds(Set.of(1L, 2L, 3L, 5L)))
            .willReturn(Map.of(1L, 501L, 2L, 501L, 3L, 502L, 5L, 502L));
        given(loadProjectMatchingRoundPort.listByChapterId(CHAPTER_ID)).willReturn(List.of(
            round(21L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST, -10, -9),
            round(11L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, -10, -9),
            round(22L, MatchingType.PLAN_DEVELOPER, MatchingPhase.SECOND, -5, -4),
            round(25L, MatchingType.PLAN_DEVELOPER, MatchingPhase.THIRD, -3, 1), randomRound()));
        given(loadProjectStatisticsPort.listPublicActiveMembersByChapterId(CHAPTER_ID)).willReturn(List.of(
            new ProjectStatisticsMatchedMemberRow(100L, 1L, ChallengerPart.WEB, 21L),
            new ProjectStatisticsMatchedMemberRow(100L, 4L, ChallengerPart.DESIGN, 11L),
            new ProjectStatisticsMatchedMemberRow(101L, 2L, ChallengerPart.IOS, 22L),
            new ProjectStatisticsMatchedMemberRow(101L, 3L, ChallengerPart.WEB, 24L),
            new ProjectStatisticsMatchedMemberRow(101L, 5L, ChallengerPart.WEB, 25L),
            new ProjectStatisticsMatchedMemberRow(101L, 5L, ChallengerPart.WEB, null),
            new ProjectStatisticsMatchedMemberRow(101L, 8L, ChallengerPart.WEB, 21L)));
        Project project = project(100L, CHAPTER_ID);
        given(loadProjectPartQuotaPort.listByProjectIdsGroupedByProjectId(Set.of(100L, 101L, 102L)))
            .willReturn(Map.of(100L, List.of(
                ProjectPartQuota.create(project, ChallengerPart.WEB, 3L, REQUESTER_ID),
                ProjectPartQuota.create(project, ChallengerPart.DESIGN, 2L, REQUESTER_ID),
                ProjectPartQuota.create(project, ChallengerPart.PLAN, 1L, REQUESTER_ID))));

        // when
        ProjectMatchingStatisticsInfo result = sut.getMatchingStatisticsByChapterId(CHAPTER_ID);

        // then
        assertThat(result.roundMatchingStatistics()).extracting("phase", "matchedMemberCount", "availableMemberCount")
            .containsExactly(tuple(MatchingPhase.FIRST, 1L, 4L), tuple(MatchingPhase.SECOND, 1L, 3L),
                tuple(MatchingPhase.THIRD, 1L, 2L), tuple(MatchingPhase.RANDOM_MATCHING, 1L, 1L));
        assertThat(result.schoolMatchingStatistics())
            .extracting("schoolId", "schoolName", "matchedMemberCount", "totalMemberCount")
            .containsExactly(tuple(501L, "가톨릭대학교", 2L, 2L), tuple(502L, "세종대학교", 2L, 2L),
                tuple(503L, "지원 가능 인원이 없는 학교", 0L, 0L));
        assertThat(result.projectMatchingStatistics()).extracting("projectId", "matchedMemberCount", "quota")
            .containsExactly(tuple(100L, 1L, 3L), tuple(101L, 3L, 0L), tuple(102L, 0L, 0L));
        var firstProject = result.projectMatchingStatistics().getFirst();
        assertThat(firstProject.projectName()).isEqualTo("프로젝트 100");
        assertThat(firstProject.parts()).extracting("part", "matchedMemberCount", "quota")
            .containsExactly(tuple(ChallengerPart.WEB, 1L, 3L));
        assertThat(result.projectMatchingStatistics().get(1).rounds()).extracting("matchedMemberCount")
            .containsExactly(0L, 1L, 1L, 1L);
        assertThat(result.projectMatchingStatistics().getLast().rounds()).extracting("matchedMemberCount")
            .containsExactly(0L, 0L, 0L, 0L);
    }

    @Test
    @DisplayName("디자인 매칭 통계는 개발 차수·인원·TO를 제외하고 지원 통계와 같은 유형을 선택한다")
    void 디자인_매칭_통계_유형_분리() {
        // given
        given(loadProjectMatchingRoundPort.listByChapterId(CHAPTER_ID)).willReturn(List.of(
            round(11L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, -10, -9),
            round(13L, MatchingType.PLAN_DESIGN, MatchingPhase.THIRD, -1, 1),
            round(21L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST, -5, -4),
            round(22L, MatchingType.PLAN_DEVELOPER, MatchingPhase.SECOND, -3, -2), randomRound()));
        given(getMemberUseCase.findAllSchoolIdsByIds(Set.of(4L))).willReturn(Map.of(4L, 501L));
        given(loadProjectStatisticsPort.listPublicProjectsByChapterId(CHAPTER_ID))
            .willReturn(List.of(targetProject(100L)));
        given(loadProjectStatisticsPort.listPublicActiveMembersByChapterId(CHAPTER_ID)).willReturn(List.of(
            new ProjectStatisticsMatchedMemberRow(100L, 4L, ChallengerPart.DESIGN, 11L),
            new ProjectStatisticsMatchedMemberRow(100L, 1L, ChallengerPart.WEB, 21L)));
        Project project = project(100L, CHAPTER_ID);
        given(loadProjectPartQuotaPort.listByProjectIdsGroupedByProjectId(Set.of(100L)))
            .willReturn(Map.of(100L, List.of(
                ProjectPartQuota.create(project, ChallengerPart.DESIGN, 2L, REQUESTER_ID),
                ProjectPartQuota.create(project, ChallengerPart.WEB, 3L, REQUESTER_ID))));

        // when
        var result = sut.getMatchingStatisticsByChapterId(CHAPTER_ID);
        var applications = sut.getApplicationStatisticsByProjectIds(List.of(100L), REQUESTER_ID);

        // then
        assertThat(applications.matchingRound().type()).isEqualTo(MatchingType.PLAN_DESIGN);
        assertThat(result.roundMatchingStatistics()).extracting("phase", "matchedMemberCount", "availableMemberCount")
            .containsExactly(tuple(MatchingPhase.FIRST, 1L, 1L), tuple(MatchingPhase.THIRD, 0L, 0L));
        assertThat(result.schoolMatchingStatistics().getFirst()).extracting("matchedMemberCount", "totalMemberCount")
            .containsExactly(1L, 1L);
        var matchingProject = result.projectMatchingStatistics().getFirst();
        assertThat(matchingProject.matchedMemberCount()).isEqualTo(1L);
        assertThat(matchingProject.quota()).isEqualTo(2L);
        assertThat(matchingProject.parts()).extracting("part", "matchedMemberCount", "quota")
            .containsExactly(tuple(ChallengerPart.DESIGN, 1L, 2L));
        assertThat(matchingProject.rounds()).extracting("phase")
            .containsExactly(MatchingPhase.FIRST, MatchingPhase.THIRD);
    }

    @Test
    @DisplayName("프로젝트가 없어도 매칭 대상 총원과 생성된 차수의 0명 통계를 반환한다")
    void 공개_프로젝트가_없는_매칭_통계() {
        // given
        given(getMemberUseCase.findAllSchoolIdsByIds(Set.of(1L, 2L, 3L)))
            .willReturn(Map.of(1L, 501L, 2L, 501L, 3L, 502L));

        // when
        var result = sut.getMatchingStatisticsByChapterId(CHAPTER_ID);

        // then
        assertThat(result.roundMatchingStatistics()).extracting("matchedMemberCount", "availableMemberCount")
            .containsExactly(tuple(0L, 3L), tuple(0L, 3L), tuple(0L, 3L));
        assertThat(result.projectMatchingStatistics()).isEmpty();
        assertThat(result.schoolMatchingStatistics()).hasSize(3);
        verifyNoInteractions(loadProjectPartQuotaPort);
    }

    @Test
    @DisplayName("매칭 지부로 이동한 챌린저의 학교는 포함하고 다른 지부로 이동한 인원은 제외한다")
    void 공개_매칭_통계에_지부_이동_반영() {
        // given
        given(matchingChapterResolver.deltaOf(CHAPTER_ID)).willReturn(new ChapterTransferDelta(
            Set.of(1L), List.of(challenger(7L, ChallengerPart.WEB))));
        given(getMemberUseCase.findAllSchoolIdsByIds(Set.of(2L, 3L, 7L)))
            .willReturn(Map.of(2L, 501L, 3L, 502L, 7L, 504L));
        given(getSchoolUseCase.listDetailsByIds(Set.of(504L))).willReturn(List.of(
            new SchoolDetailInfo(20L, "다른 지부", "이동 학교", null, 504L, null, null, List.of(), true, NOW, NOW)));

        // when
        var result = sut.getMatchingStatisticsByChapterId(CHAPTER_ID);

        // then
        assertThat(result.schoolMatchingStatistics()).extracting("schoolId", "schoolName", "totalMemberCount")
            .containsExactly(tuple(501L, "가톨릭대학교", 1L), tuple(502L, "세종대학교", 1L),
                tuple(503L, "지원 가능 인원이 없는 학교", 0L), tuple(504L, "이동 학교", 1L));
        assertThat(result.roundMatchingStatistics().getFirst().availableMemberCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("getByProjectId_보조PM이면_FULL로_멤버_단위를_포함한다")
    void 단건_보조PM이면_FULL() {
        // given
        Long projectId = 10L;
        Long gisuId = 1L;
        Long chapterId = 3L;
        Long requesterMemberId = 8300L;
        givenSingleProject(projectId, gisuId, chapterId);
        // PO 는 아니지만 ACTIVE PLAN 멤버(Sub-PM)
        given(loadProjectPort.getById(projectId))
            .willReturn(project(projectId, 9999L, chapterId));
        given(projectStatisticsAccessPolicy.canReadProjectStatistics(eq(requesterMemberId), any(Project.class)))
            .willReturn(true);

        // when
        ProjectStatisticsInfo result = sut.getByProjectId(projectId, requesterMemberId);

        // then
        assertThat(result.projectMembers()).hasSize(1);
    }

    @Test
    @DisplayName("getByProjectId_지부장이면_조회_가능하다")
    void 단건_지부장이면_조회_가능() {
        // given
        Long projectId = 10L;
        Long gisuId = 1L;
        Long chapterId = 3L;
        Long requesterMemberId = 8400L;
        givenSingleProject(projectId, gisuId, chapterId);
        given(loadProjectPort.getById(projectId))
            .willReturn(project(projectId, 9999L, chapterId));
        given(projectStatisticsAccessPolicy.canReadProjectStatistics(eq(requesterMemberId), any(Project.class)))
            .willReturn(true);

        // when
        ProjectStatisticsInfo result = sut.getByProjectId(projectId, requesterMemberId);

        // then — 권한 통과: 프로젝트 멤버까지 그대로 노출
        assertThat(result.projectMembers()).hasSize(1);
    }

    @Test
    @DisplayName("getByProjectId_권한이_없으면_PROJECT_ACCESS_DENIED")
    void 단건_권한없으면_거부() {
        // given
        Long projectId = 10L;
        Long chapterId = 3L;
        Long requesterMemberId = 8500L;
        given(loadProjectPort.getById(projectId))
            .willReturn(project(projectId, 9999L, chapterId));
        given(projectStatisticsAccessPolicy.canReadProjectStatistics(eq(requesterMemberId), any(Project.class)))
            .willReturn(false);

        // when & then
        assertThatThrownBy(() -> sut.getByProjectId(projectId, requesterMemberId))
            .isInstanceOf(ProjectDomainException.class);
    }

    @Test
    @DisplayName("같은 회원의 복수 프로젝트 지원은 전체·학교 통계에서 한 번만 집계한다")
    void 복수_프로젝트_지원_중복_제거() {
        // given
        given(loadProjectStatisticsPort.listCountedApplicationsByProjectIds(Set.of(100L, 101L))).willReturn(List.of(
            application(100L, 1L, 21L, MatchingPhase.FIRST),
            application(101L, 1L, 21L, MatchingPhase.FIRST),
            application(101L, 2L, 21L, MatchingPhase.FIRST),
            application(100L, 1L, 22L, MatchingPhase.SECOND),
            application(100L, 4L, 21L, MatchingPhase.FIRST)));

        // when
        var result = sut.getApplicationStatisticsByChapterId(CHAPTER_ID, REQUESTER_ID);

        // then
        assertThat(result.matchingRound().matchingRoundId()).isEqualTo(22L);
        assertThat(result.summary().roundApplicationStatistics())
            .extracting(RoundApplicationStatisticsInfo::appliedMemberCount).containsExactly(2L, 1L, 0L);
        var school = result.summary().schoolApplicationStatistics().getFirst();
        assertThat(school.schoolName()).isEqualTo("가톨릭대학교");
        assertThat(school.totalMemberCount()).isEqualTo(2);
        assertThat(school.rounds()).extracting(ApplicationPhaseCountInfo::applicantCount).containsExactly(2L, 1L, 0L);
        assertThat(result.summary().projectRoundStatistics().getFirst().rounds())
            .extracting(ApplicationPhaseCountInfo::applicantCount).containsExactly(1L, 1L, 0L);
        assertThat(result.summary().projectRoundStatistics().get(1).rounds())
            .extracting(ApplicationPhaseCountInfo::applicantCount).containsExactly(2L, 0L, 0L);
    }

    @Test
    @DisplayName("프로젝트 지정 시 분자는 해당 프로젝트이고 분모는 다른 프로젝트의 실제 매칭도 차감한다")
    void 지정_프로젝트_분모는_지부_전체_기준() {
        // given
        given(loadProjectStatisticsPort.listCountedApplicationsByProjectIds(Set.of(100L)))
            .willReturn(List.of(application(100L, 2L, 22L, MatchingPhase.SECOND)));
        given(loadProjectMemberPort.listMatchedRoundsByProjectIdsAndMemberIds(Set.of(100L, 101L), Set.of(1L, 2L, 3L)))
            .willReturn(List.of(
                new ProjectMemberMatchedRoundInfo(101L, 1L, 21L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST),
                new ProjectMemberMatchedRoundInfo(100L, 2L, 22L, MatchingType.PLAN_DEVELOPER, MatchingPhase.SECOND)));

        // when
        var result = sut.getApplicationStatisticsByProjectIds(List.of(100L), REQUESTER_ID);

        // then
        assertThat(result.summary().roundApplicationStatistics())
            .extracting(RoundApplicationStatisticsInfo::availableMemberCount).containsExactly(3L, 2L, 1L);
        assertThat(result.summary().roundApplicationStatistics())
            .extracting(RoundApplicationStatisticsInfo::appliedMemberCount).containsExactly(0L, 1L, 0L);
        assertThat(result.summary().projectRoundStatistics()).hasSize(1);
        assertThat(result.summary().schoolApplicationStatistics().getFirst().totalMemberCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("지원자가 없는 프로젝트와 학교도 1·2·3차를 0명으로 반환한다")
    void 지원자_없는_프로젝트와_학교() {
        // when
        var result = sut.getApplicationStatisticsByProjectIds(List.of(100L, 101L), REQUESTER_ID);

        // then
        assertThat(result.summary().projectRoundStatistics()).hasSize(2).allSatisfy(project ->
            assertThat(project.rounds()).extracting(ApplicationPhaseCountInfo::applicantCount)
                .containsExactly(0L, 0L, 0L));
        assertThat(result.summary().schoolApplicationStatistics()).hasSize(3);
        assertThat(result.summary().schoolApplicationStatistics().getLast().totalMemberCount()).isZero();
    }

    @Test
    @DisplayName("진행 중인 차수가 없으면 종료 시각이 가장 최근인 지원 차수를 사용한다")
    void 최근_종료_매칭_선택() {
        // given
        given(loadProjectMatchingRoundPort.listByChapterId(CHAPTER_ID)).willReturn(List.of(
            round(11L, MatchingType.PLAN_DESIGN, MatchingPhase.THIRD, -8, -7),
            round(21L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST, -3, -2),
            round(22L, MatchingType.PLAN_DEVELOPER, MatchingPhase.SECOND, 2, 3)));

        // when
        var result = sut.getApplicationStatisticsByChapterId(CHAPTER_ID, REQUESTER_ID);

        // then
        assertThat(result.matchingRound().matchingRoundId()).isEqualTo(21L);
        assertThat(result.matchingRound().type()).isEqualTo(MatchingType.PLAN_DEVELOPER);
    }

    @Test
    @DisplayName("디자인 매칭은 디자인 인원만 분모와 지원자 집계에 포함한다")
    void 디자인_매칭_모집단_분리() {
        // given
        given(loadProjectMatchingRoundPort.listByChapterId(CHAPTER_ID)).willReturn(List.of(
            round(11L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, -1, 1)));
        given(getMemberUseCase.findAllSchoolIdsByIds(Set.of(4L))).willReturn(Map.of(4L, 501L));
        given(loadProjectStatisticsPort.listCountedApplicationsByProjectIds(Set.of(100L))).willReturn(List.of(
            new ProjectStatisticsApplicationRow(100L, 4L, 1L, ProjectApplicationStatus.SUBMITTED,
                11L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST),
            application(100L, 1L, 21L, MatchingPhase.FIRST)));

        // when
        var result = sut.getApplicationStatisticsByProjectIds(List.of(100L), REQUESTER_ID);

        // then
        assertThat(result.summary().roundApplicationStatistics().getFirst().availableMemberCount()).isEqualTo(1L);
        assertThat(result.summary().roundApplicationStatistics().getFirst().appliedMemberCount()).isEqualTo(1L);
        assertThat(result.summary().schoolApplicationStatistics().getFirst().totalMemberCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("랜덤 매칭은 기준 차수와 지원 통계에서 제외한다")
    void 랜덤_차수_제외() {
        // given
        List<ProjectMatchingRound> rounds = List.of(
            round(23L, MatchingType.PLAN_DEVELOPER, MatchingPhase.THIRD, -3, -2),
            randomRound());
        given(loadProjectMatchingRoundPort.listByChapterId(CHAPTER_ID)).willReturn(rounds);

        // when
        var result = sut.getApplicationStatisticsByProjectIds(List.of(100L), REQUESTER_ID);

        // then
        assertThat(result.matchingRound().phase()).isEqualTo(MatchingPhase.THIRD);
        assertThat(result.summary().roundApplicationStatistics()).hasSize(1);
        assertThat(result.summary().schoolApplicationStatistics().getFirst().rounds())
            .extracting(ApplicationPhaseCountInfo::phase)
            .containsExactly(MatchingPhase.THIRD);
    }

    @Test
    @DisplayName("지원 통계의 모든 차수 배열은 생성된 기준 유형 차수만 포함하고 인원이 없으면 0명이다")
    void 지원_통계는_생성된_차수만_반환() {
        // given
        given(loadProjectMatchingRoundPort.listByChapterId(CHAPTER_ID)).willReturn(List.of(
            round(23L, MatchingType.PLAN_DEVELOPER, MatchingPhase.THIRD, 5, 6),
            round(12L, MatchingType.PLAN_DESIGN, MatchingPhase.SECOND, -5, -4),
            round(21L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST, -1, 1)));

        // when
        var result = sut.getApplicationStatisticsByProjectIds(List.of(100L), REQUESTER_ID);

        // then
        assertThat(result.summary().roundApplicationStatistics())
            .extracting(round -> round.matchingRound().phase(), RoundApplicationStatisticsInfo::appliedMemberCount)
            .containsExactly(tuple(MatchingPhase.FIRST, 0L), tuple(MatchingPhase.THIRD, 0L));
        assertThat(result.summary().schoolApplicationStatistics()).allSatisfy(school ->
            assertThat(school.rounds()).extracting("phase", "applicantCount")
                .containsExactly(tuple(MatchingPhase.FIRST, 0L), tuple(MatchingPhase.THIRD, 0L)));
        assertThat(result.summary().projectRoundStatistics()).allSatisfy(project ->
            assertThat(project.rounds()).extracting("phase", "applicantCount")
                .containsExactly(tuple(MatchingPhase.FIRST, 0L), tuple(MatchingPhase.THIRD, 0L)));
    }

    @Test
    @DisplayName("매칭 통계는 생성된 차수를 중복 없이 반환하고 진행 전 차수도 0명으로 포함한다")
    void 매칭_통계는_생성된_차수만_반환() {
        // given
        given(loadProjectMatchingRoundPort.listByChapterId(CHAPTER_ID)).willReturn(List.of(
            round(23L, MatchingType.PLAN_DEVELOPER, MatchingPhase.THIRD, 5, 6),
            round(11L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, -10, -9),
            round(21L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST, -1, 1)));
        given(loadProjectStatisticsPort.listPublicProjectsByChapterId(CHAPTER_ID))
            .willReturn(List.of(targetProject(100L)));

        // when
        var result = sut.getMatchingStatisticsByChapterId(CHAPTER_ID);

        // then
        assertThat(result.roundMatchingStatistics()).extracting("phase", "matchedMemberCount")
            .containsExactly(tuple(MatchingPhase.FIRST, 0L), tuple(MatchingPhase.THIRD, 0L));
        assertThat(result.projectMatchingStatistics().getFirst().rounds()).extracting("phase", "matchedMemberCount")
            .containsExactly(tuple(MatchingPhase.FIRST, 0L), tuple(MatchingPhase.THIRD, 0L));
    }

    @Test
    @DisplayName("생성된 매칭 차수가 없으면 전체와 프로젝트별 차수 배열이 비어 있다")
    void 매칭_차수가_없으면_빈_배열_반환() {
        // given
        given(loadProjectMatchingRoundPort.listByChapterId(CHAPTER_ID)).willReturn(List.of());

        // when
        var result = sut.getMatchingStatisticsByChapterId(CHAPTER_ID);

        // then
        assertThat(result.roundMatchingStatistics()).isEmpty();
        assertThat(result.projectMatchingStatistics()).isEmpty();
        verifyNoInteractions(loadProjectStatisticsPort, getChallengerUseCase, loadProjectPartQuotaPort);
    }

    @Test
    @DisplayName("아직 시작한 매칭이 없으면 기준 차수와 통계가 비어 있다")
    void 시작_전_빈_통계() {
        // given
        given(loadProjectMatchingRoundPort.listByChapterId(CHAPTER_ID))
            .willReturn(List.of(round(21L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST, 1, 2)));

        // when
        var result = sut.getApplicationStatisticsByChapterId(CHAPTER_ID, REQUESTER_ID);

        // then
        assertThat(result).isEqualTo(ProjectApplicationStatisticsInfo.empty(CHAPTER_ID));
        verifyNoInteractions(getChallengerUseCase, loadProjectMemberPort);
    }

    @Test
    @DisplayName("지부 조회 권한이 없으면 통계를 읽기 전에 거절한다")
    void 지부_권한_거절() {
        // given
        given(projectStatisticsAccessPolicy.canReadChapterStatistics(REQUESTER_ID, CHAPTER_ID)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> sut.getApplicationStatisticsByChapterId(CHAPTER_ID, REQUESTER_ID))
            .isInstanceOf(ProjectDomainException.class);
        verifyNoInteractions(loadProjectStatisticsPort, loadProjectMatchingRoundPort);
    }

    @Test
    @DisplayName("복수 프로젝트 중 하나라도 권한이 없으면 전체 요청을 거절한다")
    void 복수_프로젝트_각각_권한_검증() {
        // given
        var projects = List.of(project(100L, CHAPTER_ID), project(101L, CHAPTER_ID));
        given(loadProjectPort.listByIds(Set.of(100L, 101L))).willReturn(projects);
        given(projectStatisticsAccessPolicy.canReadProjectStatistics(REQUESTER_ID, projects.getLast()))
            .willReturn(false);

        // when & then
        assertThatThrownBy(() -> sut.getApplicationStatisticsByProjectIds(List.of(100L, 101L), REQUESTER_ID))
            .isInstanceOf(ProjectDomainException.class);
        verify(projectStatisticsAccessPolicy).canReadProjectStatistics(REQUESTER_ID, projects.getFirst());
        verifyNoInteractions(loadProjectMatchingRoundPort);
    }

    @Test
    @DisplayName("서로 다른 지부의 프로젝트는 함께 조회할 수 없다")
    void 다른_지부_프로젝트_거절() {
        // given
        given(loadProjectPort.listByIds(Set.of(100L, 101L)))
            .willReturn(List.of(project(100L, CHAPTER_ID), project(101L, 20L)));

        // when & then
        assertThatThrownBy(() -> sut.getApplicationStatisticsByProjectIds(List.of(100L, 101L), REQUESTER_ID))
            .isInstanceOf(ProjectDomainException.class);
        verifyNoInteractions(loadProjectMatchingRoundPort);
    }

    @Test
    @DisplayName("없는 프로젝트가 포함되면 부분 결과를 반환하지 않는다")
    void 없는_프로젝트_거절() {
        // given
        given(loadProjectPort.listByIds(Set.of(100L, 999L))).willReturn(List.of(project(100L, CHAPTER_ID)));

        // when & then
        assertThatThrownBy(() -> sut.getApplicationStatisticsByProjectIds(List.of(100L, 999L), REQUESTER_ID))
            .isInstanceOf(ProjectDomainException.class);
    }

    @Test
    @DisplayName("중복된 프로젝트 ID는 한 번만 집계한다")
    void 프로젝트_ID_중복_제거() {
        // when
        var result = sut.getApplicationStatisticsByProjectIds(List.of(100L, 100L), REQUESTER_ID);

        // then
        assertThat(result.summary().projectRoundStatistics()).hasSize(1);
    }

    /** 멤버 1명짜리 단건 프로젝트 통계 데이터 stub (집계 경로 통과용). */
    private void givenSingleProject(Long projectId, Long gisuId, Long chapterId) {
        given(loadProjectStatisticsPort.getProjectById(projectId))
            .willReturn(projectRow(projectId, gisuId, chapterId));
        given(loadProjectMatchingRoundPort.listByChapterId(chapterId)).willReturn(List.of());
        given(loadProjectStatisticsPort.listActiveMembersByProjectId(projectId))
            .willReturn(List.of(memberRow(projectId, 101L, 1001L, ChallengerPart.WEB)));
        given(loadProjectStatisticsPort.listCountedApplicationsByProjectIds(Set.of(projectId)))
            .willReturn(List.of());
        given(getChallengerUseCase.listByChapterId(chapterId))
            .willReturn(List.of(challenger(1001L, gisuId, ChallengerPart.WEB)));
        given(getMemberUseCase.findAllSchoolIdsByIds(Set.of(1001L))).willReturn(Map.of(1001L, 501L));
    }

    private static Project project(Long id, Long ownerMemberId, Long chapterId) {
        try {
            var constructor = Project.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Project project = constructor.newInstance();
            ReflectionTestUtils.setField(project, "id", id);
            ReflectionTestUtils.setField(project, "productOwnerMemberId", ownerMemberId);
            ReflectionTestUtils.setField(project, "chapterId", chapterId);
            return project;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ProjectStatisticsProjectRow projectRow(Long projectId, Long gisuId, Long chapterId) {
        return new ProjectStatisticsProjectRow(projectId, gisuId, chapterId);
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

    @Test
    @DisplayName("지부 지원 통계는 쿼리에서 선별한 프로젝트만 집계한다")
    void 지부_조회에서_미등록_프로젝트_제외() {
        // given
        given(loadProjectStatisticsPort.listApplicationStatisticsProjectsByIds(Set.of(100L, 101L)))
            .willReturn(List.of(targetProject(100L)));
        given(loadProjectStatisticsPort.listCountedApplicationsByProjectIds(Set.of(100L)))
            .willReturn(List.of(application(100L, 1L, 21L, MatchingPhase.FIRST)));

        // when
        var result = sut.getApplicationStatisticsByChapterId(CHAPTER_ID, REQUESTER_ID);

        // then
        assertThat(result.summary().projectRoundStatistics()).extracting(project -> project.projectId())
            .containsExactly(100L);
        assertThat(result.summary().roundApplicationStatistics().getFirst().appliedMemberCount()).isEqualTo(1L);
        assertThat(result.summary().schoolApplicationStatistics().getFirst().rounds().getFirst().applicantCount())
            .isEqualTo(1L);
        verify(loadProjectStatisticsPort).listCountedApplicationsByProjectIds(Set.of(100L));
    }

    @ParameterizedTest
    @EnumSource(value = ProjectStatus.class, names = {"DRAFT", "PENDING_REVIEW"})
    @DisplayName("프로젝트를 직접 지정해도 임시저장·승인대기는 지원 통계에서 제외한다")
    void 프로젝트_지정_조회에서도_미등록_프로젝트_제외(ProjectStatus status) {
        // given
        Project excluded = project(101L, CHAPTER_ID);
        ReflectionTestUtils.setField(excluded, "status", status);
        given(loadProjectPort.listByIds(Set.of(100L, 101L)))
            .willReturn(List.of(project(100L, CHAPTER_ID), excluded));
        given(loadProjectStatisticsPort.listApplicationStatisticsProjectsByIds(Set.of(100L, 101L)))
            .willReturn(List.of(targetProject(100L)));
        given(loadProjectStatisticsPort.listCountedApplicationsByProjectIds(Set.of(100L)))
            .willReturn(List.of(application(100L, 1L, 21L, MatchingPhase.FIRST)));

        // when
        var result = sut.getApplicationStatisticsByProjectIds(List.of(100L, 101L), REQUESTER_ID);

        // then
        assertThat(result.summary().projectRoundStatistics()).extracting(project -> project.projectId())
            .containsExactly(100L);
        assertThat(result.summary().roundApplicationStatistics().getFirst().appliedMemberCount()).isEqualTo(1L);
        verify(loadProjectStatisticsPort).listCountedApplicationsByProjectIds(Set.of(100L));
    }

    private static ProjectStatisticsTargetProjectRow targetProject(Long id) {
        return new ProjectStatisticsTargetProjectRow(id, "프로젝트 " + id);
    }

    private static Project project(Long id, Long chapterId) {
        Project project = Project.createDraft(GISU_ID, chapterId, REQUESTER_ID, 501L, REQUESTER_ID);
        ReflectionTestUtils.setField(project, "id", id);
        ReflectionTestUtils.setField(project, "name", "프로젝트 " + id);
        ReflectionTestUtils.setField(project, "status", ProjectStatus.IN_PROGRESS);
        return project;
    }

    private static ProjectMatchingRound round(Long id, MatchingType type, MatchingPhase phase, long start, long end) {
        return round(id, type, phase, start, end, CHAPTER_ID);
    }

    private static ProjectMatchingRound round(
        Long id, MatchingType type, MatchingPhase phase, long start, long end, Long chapterId
    ) {
        Instant endsAt = NOW.plus(end, ChronoUnit.DAYS);
        ProjectMatchingRound round = ProjectMatchingRound.create("매칭", null, type, phase, chapterId,
            NOW.plus(start, ChronoUnit.DAYS), endsAt, endsAt.plusSeconds(60), null);
        ReflectionTestUtils.setField(round, "id", id);
        return round;
    }

    private static ProjectMatchingRound randomRound() {
        Instant endsAt = NOW.plusSeconds(60);
        ProjectMatchingRound round = ProjectMatchingRound.create("랜덤", null, MatchingType.PLAN_DEVELOPER,
            MatchingPhase.RANDOM_MATCHING, CHAPTER_ID,
            NOW.minusSeconds(60), endsAt, endsAt, endsAt.plus(5, ChronoUnit.MINUTES));
        ReflectionTestUtils.setField(round, "id", 24L);
        return round;
    }

    private static ChallengerInfo challenger(Long memberId, ChallengerPart part) {
        return ChallengerInfo.builder().memberId(memberId).gisuId(GISU_ID).part(part)
            .challengerStatus(ChallengerStatus.ACTIVE).build();
    }

    private static ProjectStatisticsApplicationRow application(Long projectId, Long memberId, Long roundId,
                                                              MatchingPhase phase) {
        return new ProjectStatisticsApplicationRow(projectId, memberId, projectId * 100 + memberId,
            ProjectApplicationStatus.SUBMITTED, roundId, MatchingType.PLAN_DEVELOPER, phase);
    }
}
