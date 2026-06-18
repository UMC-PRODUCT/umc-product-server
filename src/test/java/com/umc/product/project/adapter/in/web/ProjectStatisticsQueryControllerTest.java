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
import com.umc.product.project.adapter.in.web.dto.response.statistics.ChapterProjectMatchingStatisticsResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ChapterProjectMatchingStatisticsResponse.ProjectMatchingCountResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ChapterProjectMatchingStatisticsResponse.RoundMatchingStatisticsResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ChapterProjectMatchingStatisticsResponse.UnclassifiedMatchingStatisticsResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ChapterProjectStatisticsResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ChapterProjectStatisticsResponse.ChapterProjectStatisticsSummaryResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ChapterProjectStatisticsResponse.ProjectRoundMemberCountResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ChapterProjectStatisticsResponse.ProjectRoundMemberStatisticsResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ChapterProjectStatisticsResponse.SchoolMatchingStatisticsResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ProjectStatisticsResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ProjectStatisticsResponse.ProjectMatchingRoundStatisticsResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ProjectStatisticsResponse.ProjectMemberApplicationStatisticsResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ProjectStatisticsResponse.ProjectMemberStatisticsResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ProjectStatisticsResponse.RoundApplicationStatisticsResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ProjectStatisticsResponse.RoundSchoolApplicationStatisticsResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ProjectStatisticsResponse.SchoolApplicationStatisticsResponse;
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

    @Test
    @DisplayName("GET_statistics_projectId_단건_프로젝트_지원_매칭_현황을_반환한다")
    void 통합_경로_단건_프로젝트_통계_조회() throws Exception {
        // given
        given(assembler.statisticsForProject(10L, TEST_MEMBER_ID))
            .willReturn(response(10L, 101L, 1001L, 201L));

        // when & then
        mockMvc.perform(get("/api/v1/projects/statistics")
                .param("projectId", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.projectId").value(10L))
            .andExpect(jsonPath("$.result.projectMembers[0].projectMemberId").value(101L))
            .andExpect(jsonPath("$.result.roundApplicationStatistics[0].appliedMemberCount").value(1));
    }

    @Test
    @DisplayName("GET_statistics_chapterId_지부_전체_프로젝트_지원_매칭_현황을_반환한다")
    void 지부_전체_통계_조회() throws Exception {
        // given
        given(assembler.statisticsForChapter(3L, TEST_MEMBER_ID))
            .willReturn(chapterResponse(3L, List.of(
                response(10L, 101L, 1001L, 201L),
                response(11L, 102L, 1002L, 202L)
            )));

        // when & then
        mockMvc.perform(get("/api/v1/projects/statistics")
                .param("chapterId", "3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.chapterId").value(3L))
            .andExpect(jsonPath("$.result.projects[0].projectId").value(10L))
            .andExpect(jsonPath("$.result.projects[1].projectId").value(11L))
            .andExpect(jsonPath("$.result.summary.roundApplicationStatistics[0].appliedMemberCount").value(2))
            .andExpect(jsonPath("$.result.summary.schoolMatchingStatistics[0].matchedMemberCount").value(1))
            .andExpect(jsonPath("$.result.summary.projectRoundStatistics[0].matchingRounds[0].appliedMemberCount")
                .value(2))
            .andExpect(jsonPath("$.result.summary.projectRoundStatistics[0].matchingRounds[0].matchedMemberCount")
                .value(1));
    }

    @Test
    @DisplayName("GET_statistics_projectId_chapterId_둘_다_제공하면_400을_반환한다")
    void 통합_통계_조회_조건_중복이면_400() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/projects/statistics")
                .param("projectId", "10")
                .param("chapterId", "3"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET_statistics_projectId_chapterId_둘_다_없으면_400을_반환한다")
    void 통합_통계_조회_조건_누락이면_400() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/projects/statistics"))
            .andExpect(status().isBadRequest());
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
            .andExpect(jsonPath("$.result.roundMatchingStatistics[0].projects[0].projectId").value(10L))
            .andExpect(jsonPath("$.result.schoolMatchingStatistics[0].matchedMemberCount").value(2))
            .andExpect(jsonPath("$.result.unclassifiedMatchingStatistics.matchedMemberCount").value(1));
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

    private static ChapterProjectStatisticsResponse chapterResponse(
        Long chapterId,
        List<ProjectStatisticsResponse> projects
    ) {
        ProjectMatchingRoundStatisticsResponse round = new ProjectMatchingRoundStatisticsResponse(
            1L,
            MatchingType.PLAN_DEVELOPER,
            MatchingPhase.FIRST
        );

        return new ChapterProjectStatisticsResponse(
            chapterId,
            projects,
            new ChapterProjectStatisticsSummaryResponse(
                List.of(new RoundApplicationStatisticsResponse(round, 2L, 4L)),
                List.of(new RoundSchoolApplicationStatisticsResponse(
                    round,
                    List.of(new SchoolApplicationStatisticsResponse(501L, 2L))
                )),
                List.of(new SchoolMatchingStatisticsResponse(501L, 1L, 2L)),
                List.of(new ProjectRoundMemberStatisticsResponse(
                    projects.get(0).projectId(),
                    List.of(new ProjectRoundMemberCountResponse(round, 2L, 1L))
                ))
            )
        );
    }

    private static ChapterProjectMatchingStatisticsResponse matchingResponse(Long chapterId) {
        ProjectMatchingRoundStatisticsResponse round = new ProjectMatchingRoundStatisticsResponse(
            1L,
            MatchingType.PLAN_DEVELOPER,
            MatchingPhase.FIRST
        );

        return new ChapterProjectMatchingStatisticsResponse(
            chapterId,
            List.of(new RoundMatchingStatisticsResponse(
                round,
                2L,
                4L,
                List.of(new ProjectMatchingCountResponse(10L, 2L))
            )),
            List.of(new ChapterProjectMatchingStatisticsResponse.SchoolMatchingStatisticsResponse(
                501L,
                2L,
                3L
            )),
            new UnclassifiedMatchingStatisticsResponse(
                1L,
                List.of(new ProjectMatchingCountResponse(11L, 1L))
            )
        );
    }
}
