package com.umc.product.project.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import com.umc.product.global.response.PageResponse;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.project.adapter.in.web.assembler.ProjectResponseAssembler;
import com.umc.product.project.adapter.in.web.dto.response.ProjectMatchingOverviewResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectSummaryResponse;
import com.umc.product.project.application.port.in.query.dto.ProjectMemberView;
import com.umc.product.project.application.port.in.query.dto.SearchProjectQuery;

@WebMvcTest(controllers = ProjectQueryController.class)
@Import(JacksonConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectQueryControllerTest {

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
    void 매칭_현황_표_기본_조회() throws Exception {
        // given
        given(assembler.matchingOverviewForChapter(3L, ProjectMemberView.PUBLISHED, TEST_MEMBER_ID))
            .willReturn(new ProjectMatchingOverviewResponse(List.of()));

        // when & then
        mockMvc.perform(get("/api/v1/projects/matching-overview").param("chapterId", "3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.projects").isEmpty())
            .andExpect(jsonPath("$.result.page").doesNotExist());
    }

    @Test
    void 매칭_현황_표_조정_조회() throws Exception {
        // given
        given(assembler.matchingOverviewForChapter(3L, ProjectMemberView.ADJUSTMENT, TEST_MEMBER_ID))
            .willReturn(new ProjectMatchingOverviewResponse(List.of()));

        // when & then
        mockMvc.perform(get("/api/v1/projects/matching-overview")
                .param("chapterId", "3").param("view", "ADJUSTMENT"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.projects").isArray());
    }

    @Test
    void 매칭_현황_표_잘못된_요청() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/projects/matching-overview"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/projects/matching-overview")
                .param("chapterId", "3").param("view", "UNKNOWN"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void GET_프로젝트_목록은_추가_파라미터_없이_팀매칭_정렬을_사용한다() throws Exception {
        given(assembler.searchFor(any(SearchProjectQuery.class), eq(TEST_MEMBER_ID)))
            .willReturn(new PageResponse<ProjectSummaryResponse>(List.of(), 0, 20, 0, 0, false, false));

        mockMvc.perform(get("/api/v1/projects")
                .param("gisuId", "1"))
            .andExpect(status().isOk());

        ArgumentCaptor<SearchProjectQuery> captor = ArgumentCaptor.forClass(SearchProjectQuery.class);
        then(assembler).should().searchFor(captor.capture(), eq(TEST_MEMBER_ID));

        assertThat(captor.getValue().matchingListOrder()).isTrue();
        assertThat(captor.getValue().matchingOrderAt()).isNull();
    }

    @Test
    void 추가_옵션_없이_팀매칭_정렬을_적용하고_기존_파트필터를_전달한다() throws Exception {
        // given
        given(assembler.searchFor(any(SearchProjectQuery.class), eq(TEST_MEMBER_ID)))
            .willReturn(new PageResponse<ProjectSummaryResponse>(List.of(), 0, 20, 0, 0, false, false));

        // when
        mockMvc.perform(get("/api/v1/projects").param("gisuId", "1")
                .param("parts", "DESIGN").param("matchingOrderAt", "2099-01-01T00:00:00Z"))
            .andExpect(status().isOk());

        // then
        ArgumentCaptor<SearchProjectQuery> captor = ArgumentCaptor.forClass(SearchProjectQuery.class);
        then(assembler).should().searchFor(captor.capture(), eq(TEST_MEMBER_ID));
        assertThat(captor.getValue().matchingListOrder()).isTrue();
        assertThat(captor.getValue().parts()).containsExactly(ChallengerPart.DESIGN);
        assertThat(captor.getValue().matchingOrderAt()).isNull();
    }

    @Test
    void GET_프로젝트_목록_비회원도_조회_가능() throws Exception {
        SecurityContextHolder.clearContext();
        given(assembler.searchFor(any(SearchProjectQuery.class), isNull()))
            .willReturn(new PageResponse<ProjectSummaryResponse>(List.of(), 0, 20, 0, 0, false, false));

        mockMvc.perform(get("/api/v1/projects")
                .param("gisuId", "1"))
            .andExpect(status().isOk());

        then(assembler).should().searchFor(any(SearchProjectQuery.class), isNull());
    }

    @Test
    void GET_프로젝트_상세_비회원도_조회_가능() throws Exception {
        SecurityContextHolder.clearContext();
        given(assembler.detailFor(42L)).willReturn(null);

        mockMvc.perform(get("/api/v1/projects/42"))
            .andExpect(status().isOk());

        then(assembler).should().detailFor(42L);
    }
    @Test
    void 내_초안_목록은_로그인한_작성자의_페이지만_조회한다() throws Exception {
        given(assembler.draftsFor(eq(TEST_MEMBER_ID), eq(1L), any()))
            .willReturn(new PageResponse<>(List.of(), 1, 2, 0, 0, false, true));

        mockMvc.perform(get("/api/v1/projects/me/drafts")
                .param("gisuId", "1").param("page", "1").param("size", "2")
                .param("creatorMemberId", "1234"))
            .andExpect(status().isOk());
        then(assembler).should().draftsFor(eq(TEST_MEMBER_ID), eq(1L), any());
    }

}
