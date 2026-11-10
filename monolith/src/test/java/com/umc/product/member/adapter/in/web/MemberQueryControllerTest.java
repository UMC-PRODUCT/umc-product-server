package com.umc.product.member.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

import com.umc.product.authorization.application.port.in.query.CheckChallengerAuthorityUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.global.config.JacksonConfig;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.member.adapter.in.web.assembler.MemberInfoResponseAssembler;
import com.umc.product.member.application.port.in.query.GetMemberProfileUseCase;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.SearchMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.member.application.port.in.query.dto.MemberProfileInfo;
import com.umc.product.member.application.port.in.query.dto.SearchMemberItemInfo;
import com.umc.product.member.application.port.in.query.dto.SearchMemberResult;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;

@WebMvcTest(controllers = MemberQueryController.class)
@Import({JacksonConfig.class, MemberInfoResponseAssembler.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("MemberQueryController")
class MemberQueryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    GetMemberUseCase getMemberUseCase;

    @MockitoBean
    GetMemberProfileUseCase getMemberProfileUseCase;

    @MockitoBean
    GetChallengerUseCase getChallengerUseCase;

    @MockitoBean
    GetGisuUseCase getGisuUseCase;

    @MockitoBean
    GetChapterUseCase getChapterUseCase;

    @MockitoBean
    CheckChallengerAuthorityUseCase checkChallengerAuthorityUseCase;

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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("챌린저 이력이 없어도 내 프로필에 프로덕트 권한 판정을 반환한다")
    void 챌린저_이력이_없어도_내_프로필에_프로덕트_권한_판정을_반환한다(boolean isUmcProduct) throws Exception {
        // given
        given(getMemberUseCase.getById(99L)).willReturn(MemberInfo.builder()
            .id(99L)
            .name("홍길동")
            .status(MemberStatus.ACTIVE)
            .roles(List.of())
            .build());
        given(getMemberProfileUseCase.getMemberProfileById(99L)).willReturn(MemberProfileInfo.builder().build());
        given(getChallengerUseCase.getAllByMemberId(99L)).willReturn(List.of());
        given(checkChallengerAuthorityUseCase.isUmcProduct(99L)).willReturn(isUmcProduct);

        // when / then
        mockMvc.perform(get("/api/v1/member/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.id").value(99L))
            .andExpect(jsonPath("$.result.name").value("홍길동"))
            .andExpect(jsonPath("$.result.roles").isEmpty())
            .andExpect(jsonPath("$.result.challengerRecords").isEmpty())
            .andExpect(jsonPath("$.result.isUmcProduct").value(isUmcProduct));

        then(checkChallengerAuthorityUseCase).should().isUmcProduct(99L);
    }

    @Test
    @DisplayName("타인 프로필은 프로덕트 권한을 조회하거나 노출하지 않는다")
    void 타인_프로필은_프로덕트_권한을_조회하거나_노출하지_않는다() throws Exception {
        // given
        given(getMemberUseCase.getById(100L)).willReturn(MemberInfo.builder()
            .id(100L)
            .name("김철수")
            .email("chulsu@example.com")
            .status(MemberStatus.ACTIVE)
            .roles(List.of())
            .build());
        given(getMemberProfileUseCase.getPublicMemberProfileById(100L))
            .willReturn(MemberProfileInfo.builder().build());
        given(getChallengerUseCase.getAllByMemberId(100L)).willReturn(List.of());

        // when / then
        mockMvc.perform(get("/api/v1/member/profile/100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.id").value(100L))
            .andExpect(jsonPath("$.result.email").isEmpty())
            .andExpect(jsonPath("$.result.isUmcProduct").doesNotHaveJsonPath());

        then(checkChallengerAuthorityUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("회원 검색 응답은 이메일을 마스킹한다")
    void 회원_검색_응답은_이메일을_마스킹한다() throws Exception {
        given(searchMemberUseCase.searchBy(any(), any(), any())).willReturn(new SearchMemberResult(
            new PageImpl<>(
                List.of(new SearchMemberItemInfo(
                    1L, "홍길동", "길동", "gildong@example.com",
                    10L, "테스트대학교", null, 100L, 9L, 10L,
                    ChallengerPart.SPRINGBOOT, List.of()
                )),
                PageRequest.of(0, 10),
                1
            )
        ));

        mockMvc.perform(get("/api/v1/member/search")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.page.content[0].email").value("gil****@example.com"));
    }

    @Test
    @DisplayName("회원 검색은 현재 로그인 memberId를 별도 인자로 전달한다")
    void 회원_검색은_현재_로그인_memberId를_별도_인자로_전달한다() throws Exception {
        given(searchMemberUseCase.searchBy(any(), any(), any())).willReturn(new SearchMemberResult(
            new PageImpl<>(List.of(), PageRequest.of(0, 10), 0)
        ));

        mockMvc.perform(get("/api/v1/member/search")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk());

        then(searchMemberUseCase).should().searchBy(any(), eq(99L), any());
    }

    @Test
    @DisplayName("회원 검색 권한이 없으면 403을 반환한다")
    void 회원_검색_권한이_없으면_403을_반환한다() throws Exception {
        given(searchMemberUseCase.searchBy(any(), any(), any()))
            .willThrow(new MemberDomainException(MemberErrorCode.MEMBER_SEARCH_ACCESS_DENIED));

        mockMvc.perform(get("/api/v1/member/search")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isForbidden());
    }
}
