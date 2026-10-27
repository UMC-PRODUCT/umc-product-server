package com.umc.product.project.adapter.in.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.config.JacksonConfig;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.project.adapter.in.web.assembler.ProjectResponseAssembler;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ProjectApplicationStatisticsResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ProjectMatchingRoundStatisticsResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ProjectMatchingStatisticsResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ProjectStatisticsResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ProjectStatisticsResponse.ProjectMemberApplicationStatisticsResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ProjectStatisticsResponse.ProjectMemberStatisticsResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ProjectStatisticsResponse.RoundSchoolApplicationStatisticsResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ProjectStatisticsResponse.SchoolApplicationStatisticsResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.RoundApplicationStatisticsResponse;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectApplicationStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectApplicationStatisticsInfo.PhaseApplicantCountInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectApplicationStatisticsInfo.SchoolStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectApplicationStatisticsInfo.SummaryInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectMatchingRoundStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectMatchingStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectMatchingStatisticsInfo.PartStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectMatchingStatisticsInfo.PhaseMatchingCountInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.RoundApplicationStatisticsInfo;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.enums.ProjectMemberStatus;

@WebMvcTest(controllers = ProjectStatisticsQueryController.class)
@Import(JacksonConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectStatisticsQueryControllerTest {

    private static final Long TEST_MEMBER_ID = 99L;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    ProjectResponseAssembler assembler;

    @BeforeEach
    void setUpSecurityContext() {
        MemberPrincipal principal = MemberPrincipal.builder()
            .memberId(TEST_MEMBER_ID)
            .build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }


    @Test
    @DisplayName("지부 지원 통계는 기준 매칭과 중복 없는 요약을 반환한다")
    void 지부_지원_통계_조회() throws Exception {
        // given
        given(assembler.applicationStatisticsForChapter(3L, TEST_MEMBER_ID)).willReturn(applicationResponse());

        // when & then
        mockMvc.perform(get("/api/v1/projects/statistics/applications").param("chapterId", "3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.chapterId").value("3"))
            .andExpect(jsonPath("$.result.matchingRound.type").value("PLAN_DEVELOPER"))
            .andExpect(jsonPath("$.result.matchingRound.phase").value("SECOND"))
            .andExpect(jsonPath("$.result.summary.roundApplicationStatistics[0].availableMemberCount").value("4"))
            .andExpect(jsonPath("$.result.summary.schoolApplicationStatistics[0].schoolName").value("가톨릭대학교"))
            .andExpect(jsonPath("$.result.summary.schoolApplicationStatistics[0].totalMemberCount").value("4"))
            .andExpect(jsonPath("$.result.summary.schoolApplicationStatistics[0].rounds[0].applicantCount").value("2"))
            .andExpect(jsonPath("$.result.summary.projectRoundStatistics[0].projectName").value("프로젝트 A"))
            .andExpect(jsonPath("$.result.projects").doesNotExist())
            .andExpect(jsonPath("$.result.summary.roundSchoolRankings").doesNotExist())
            .andExpect(jsonPath("$.result.summary.schoolMatchingStatistics").doesNotExist());
    }

    @Test
    @DisplayName("단일 프로젝트도 지부 조회와 같은 응답 구조를 사용한다")
    void 단일_프로젝트_지원_통계_조회() throws Exception {
        // given
        given(assembler.applicationStatisticsForProjects(List.of(10L), TEST_MEMBER_ID))
            .willReturn(applicationResponse());

        // when & then
        mockMvc.perform(get("/api/v1/projects/statistics/applications").param("projectIds", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.summary.projectRoundStatistics[0].projectId").value("10"))
            .andExpect(jsonPath("$.result.projects").doesNotExist());
    }

    @Test
    @DisplayName("복수 프로젝트 ID를 같은 지원 통계 경로로 전달한다")
    void 복수_프로젝트_지원_통계_조회() throws Exception {
        // given
        given(assembler.applicationStatisticsForProjects(List.of(10L, 11L), TEST_MEMBER_ID))
            .willReturn(applicationResponse());

        // when & then
        mockMvc.perform(get("/api/v1/projects/statistics/applications").param("projectIds", "10,11"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.summary.schoolApplicationStatistics").isArray());
    }

    @Test
    @DisplayName("기준 매칭이 없으면 빈 통계를 반환한다")
    void 시작한_매칭_없음() throws Exception {
        // given
        given(assembler.applicationStatisticsForChapter(3L, TEST_MEMBER_ID))
            .willReturn(ProjectApplicationStatisticsResponse.from(ProjectApplicationStatisticsInfo.empty(3L)));

        // when & then
        mockMvc.perform(get("/api/v1/projects/statistics/applications").param("chapterId", "3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.matchingRound").doesNotExist())
            .andExpect(jsonPath("$.result.summary.roundApplicationStatistics").isEmpty())
            .andExpect(jsonPath("$.result.summary.schoolApplicationStatistics").isEmpty())
            .andExpect(jsonPath("$.result.summary.projectRoundStatistics").isEmpty());
    }

    @Test
    @DisplayName("조회 대상을 동시에 지정하거나 생략하면 거절한다")
    void 조회_대상_검증() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/projects/statistics/applications")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/projects/statistics/applications")
                .param("chapterId", "3").param("projectIds", "10"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET_projectId_statistics_단건_프로젝트_지원_매칭_현황을_반환한다")
    void 단건_프로젝트_통계_조회() throws Exception {
        // given
        given(assembler.statisticsForProject(10L, TEST_MEMBER_ID))
            .willReturn(response(10L, 101L, 1001L, 201L));

        // when & then
        mockMvc.perform(get("/api/v1/projects/{projectId}/statistics", 10L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.projectId").value(10L))
            .andExpect(jsonPath("$.result.projectMembers[0].projectMemberId").value(101L))
            .andExpect(jsonPath("$.result.projectMembers[0].applications[0].applicationId").value(201L))
            .andExpect(jsonPath("$.result.projectMembers[0].applications[0].matchingRound.type")
                .value("PLAN_DEVELOPER"))
            .andExpect(jsonPath("$.result.projectMembers[0].applications[0].matchingRound.phase")
                .value("FIRST"))
            .andExpect(jsonPath("$.result.roundApplicationStatistics[0].appliedMemberCount").value(1))
            .andExpect(jsonPath("$.result.roundApplicationStatistics[0].availableMemberCount").value(4))
            .andExpect(jsonPath("$.result.schoolApplicationStatistics[0].schools[0].schoolId").value(501));
    }

    private static ProjectStatisticsResponse response(
        Long projectId,
        Long projectMemberId,
        Long memberId,
        Long applicationId
    ) {
        ProjectMatchingRoundStatisticsResponse round = new ProjectMatchingRoundStatisticsResponse(
            1L,
            MatchingType.PLAN_DEVELOPER,
            MatchingPhase.FIRST
        );

        return new ProjectStatisticsResponse(
            projectId,
            List.of(new ProjectMemberStatisticsResponse(
                projectMemberId,
                memberId,
                ChallengerPart.WEB,
                ProjectMemberStatus.ACTIVE,
                List.of(new ProjectMemberApplicationStatisticsResponse(
                    applicationId,
                    ProjectApplicationStatus.SUBMITTED,
                    round
                ))
            )),
            List.of(new RoundApplicationStatisticsResponse(round, 1L, 4L)),
            List.of(new RoundSchoolApplicationStatisticsResponse(
                round,
                List.of(new SchoolApplicationStatisticsResponse(501L, 1L))
            ))
        );
    }

    private static ProjectApplicationStatisticsResponse applicationResponse() {
        ProjectMatchingRoundStatisticsInfo round =
            new ProjectMatchingRoundStatisticsInfo(2L, MatchingType.PLAN_DEVELOPER, MatchingPhase.SECOND);
        List<PhaseApplicantCountInfo> counts = List.of(
            new PhaseApplicantCountInfo(MatchingPhase.FIRST, 2),
            new PhaseApplicantCountInfo(MatchingPhase.SECOND, 1),
            new PhaseApplicantCountInfo(MatchingPhase.THIRD, 0));
        return ProjectApplicationStatisticsResponse.from(new ProjectApplicationStatisticsInfo(3L, round,
            new SummaryInfo(List.of(new RoundApplicationStatisticsInfo(round, 1, 4)),
                List.of(new SchoolStatisticsInfo(501L, "가톨릭대학교", 4, counts)),
                List.of(new ProjectApplicationStatisticsInfo.ProjectStatisticsInfo(10L, "프로젝트 A", counts)))));
    }

    @Test
    @DisplayName("GET_statistics_matchings_chapterId_공개_프로젝트_매칭_요약을_반환한다")
    void 공개_프로젝트_매칭_요약_조회() throws Exception {
        // given
        given(assembler.matchingStatisticsForChapter(3L))
            .willReturn(matchingResponse(3L));

        // when & then
        mockMvc.perform(get("/api/v1/projects/statistics/matchings")
                .param("chapterId", "3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.chapterId").value(3L))
            .andExpect(jsonPath("$.result.roundMatchingStatistics[0].matchedMemberCount").value(2))
            .andExpect(jsonPath("$.result.roundMatchingStatistics[0].availableMemberCount").value(4))
            .andExpect(jsonPath("$.result.projectMatchingStatistics[0].projectId").value(10L))
            .andExpect(jsonPath("$.result.schoolMatchingStatistics[0].matchedMemberCount").value(2))
            .andExpect(jsonPath("$.result.roundMatchingStatistics[0].phase").value("FIRST"))
            .andExpect(jsonPath("$.result.schoolMatchingStatistics[0].schoolName").value("학교"))
            .andExpect(jsonPath("$.result.projectMatchingStatistics[0].projectName").value("프로젝트"))
            .andExpect(jsonPath("$.result.projectMatchingStatistics[0].quota").value(4))
            .andExpect(jsonPath("$.result.projectMatchingStatistics[0].rounds[3].phase").value("RANDOM_MATCHING"))
            .andExpect(jsonPath("$.result.projectMatchingStatistics[0].parts[0].matchedMemberCount").value(2))
            .andExpect(jsonPath("$.result.projectMatchingStatistics[0].parts[0].quota").value(4))
            .andExpect(jsonPath("$.result.summary").doesNotExist())
            .andExpect(jsonPath("$.result.matchingRound").doesNotExist())
            .andExpect(jsonPath("$.result.roundMatchingStatistics[0].matchingRound").doesNotExist())
            .andExpect(jsonPath("$.result.matchingRate").doesNotExist())
            .andExpect(jsonPath("$.result.totalMemberCount").doesNotExist())
            .andExpect(jsonPath("$.result.matchedMemberCount").doesNotExist())
            .andExpect(jsonPath("$.result.unclassifiedMatchingStatistics").doesNotExist());
    }

    private static ProjectMatchingStatisticsResponse matchingResponse(Long chapterId) {
        return ProjectMatchingStatisticsResponse.from(new ProjectMatchingStatisticsInfo(
            chapterId,
            List.of(new ProjectMatchingStatisticsInfo.PhaseStatisticsInfo(MatchingPhase.FIRST, 2L, 4L),
                new ProjectMatchingStatisticsInfo.PhaseStatisticsInfo(MatchingPhase.SECOND, 0L, 2L),
                new ProjectMatchingStatisticsInfo.PhaseStatisticsInfo(MatchingPhase.THIRD, 0L, 2L),
                new ProjectMatchingStatisticsInfo.PhaseStatisticsInfo(MatchingPhase.RANDOM_MATCHING, 0L, 2L)),
            List.of(new ProjectMatchingStatisticsInfo.SchoolStatisticsInfo(501L, "학교", 2L, 4L)),
            List.of(new ProjectMatchingStatisticsInfo.ProjectStatisticsInfo(10L, "프로젝트", 2L, 4L,
                List.of(new PhaseMatchingCountInfo(MatchingPhase.FIRST, 2L),
                    new PhaseMatchingCountInfo(MatchingPhase.SECOND, 0L),
                    new PhaseMatchingCountInfo(MatchingPhase.THIRD, 0L),
                    new PhaseMatchingCountInfo(MatchingPhase.RANDOM_MATCHING, 0L)),
                List.of(new PartStatisticsInfo(ChallengerPart.WEB, 2L, 4L))))));
    }
}
