package com.umc.product.challenger.adapter.in.web.v2;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.global.config.JacksonConfig;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.member.application.port.in.query.SearchMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.ChallengerSearchItemV2Info;
import com.umc.product.member.application.port.in.query.dto.ChallengerSearchV2Result;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;

@WebMvcTest(controllers = ChallengerAdminSearchV2Controller.class)
@Import(JacksonConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ChallengerAdminSearchV2Controller")
class ChallengerAdminSearchV2ControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    SearchMemberUseCase searchMemberUseCase;

    @BeforeEach
    void setUpSecurityContext() {
        MemberPrincipal principal = MemberPrincipal.builder()
            .memberId(99L)
            .build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    @Test
    @DisplayName("관리자용 챌린저 검색은 권한 범위가 적용된 검색을 호출하고 이메일을 마스킹한다")
    void 관리자용_챌린저_검색은_권한_범위가_적용된_검색을_호출하고_이메일을_마스킹한다() throws Exception {
        given(searchMemberUseCase.searchChallengersByV2WithScope(any(), any(), any()))
            .willReturn(new ChallengerSearchV2Result(
                new PageImpl<>(
                    List.of(new ChallengerSearchItemV2Info(
                        1L, "홍길동", "길동", "gildong@example.com",
                        10L, "테스트대학교", null, 100L, 9L, 10L,
                        ChallengerPart.SPRINGBOOT, ChallengerStatus.ACTIVE, List.of(), false
                    )),
                    PageRequest.of(0, 10),
                    1
                )
            ));

        mockMvc.perform(get("/api/v2/challenger/admin/search")
                .param("gisuId", "9")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.page.content[0].email").value("gil****@example.com"));

        then(searchMemberUseCase).should().searchChallengersByV2WithScope(
            argThat(query -> query.gisuId().equals(9L)),
            eq(99L),
            any()
        );
    }

    @Test
    @DisplayName("관리자용 챌린저 검색의 기본 페이지 크기는 10이다")
    void 관리자용_챌린저_검색의_기본_페이지_크기는_10이다() throws Exception {
        given(searchMemberUseCase.searchChallengersByV2WithScope(any(), any(), any()))
            .willReturn(new ChallengerSearchV2Result(
                new PageImpl<>(List.of(), PageRequest.of(0, 10), 0)
            ));

        mockMvc.perform(get("/api/v2/challenger/admin/search"))
            .andExpect(status().isOk());

        then(searchMemberUseCase).should().searchChallengersByV2WithScope(
            any(),
            eq(99L),
            argThat(pageable -> pageable.getPageNumber() == 0 && pageable.getPageSize() == 10)
        );
    }

    @Test
    @DisplayName("관리자용 챌린저 검색 권한이 없으면 403을 반환한다")
    void 관리자용_챌린저_검색_권한이_없으면_403을_반환한다() throws Exception {
        given(searchMemberUseCase.searchChallengersByV2WithScope(any(), any(), any()))
            .willThrow(new MemberDomainException(MemberErrorCode.CHALLENGER_MANAGEMENT_SEARCH_ACCESS_DENIED));

        mockMvc.perform(get("/api/v2/challenger/admin/search")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("MEMBER-0015"));
    }
}
