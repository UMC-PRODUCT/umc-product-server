package com.umc.product.member.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.global.config.JacksonConfig;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.member.adapter.in.web.assembler.MemberInfoResponseAssembler;
import com.umc.product.member.adapter.in.web.dto.response.MemberInfoResponse;
import com.umc.product.member.application.port.in.command.ManageMemberProfileUseCase;
import com.umc.product.member.application.port.in.command.ManageMemberUseCase;
import com.umc.product.member.application.port.in.command.RegisterEmailMemberUseCase;
import com.umc.product.member.application.port.in.command.RegisterOAuthMemberUseCase;
import com.umc.product.member.application.port.in.command.dto.EmailRegisterMemberCommand;
import com.umc.product.member.application.port.in.command.dto.UpdateMemberCommand;

@WebMvcTest(controllers = MemberCommandController.class)
@Import(JacksonConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("MemberCommandController")
class MemberCommandControllerTest {

    private static final Long TEST_MEMBER_ID = 99L;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    MemberInfoResponseAssembler assembler;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    ManageMemberUseCase manageMemberUseCase;

    @MockitoBean
    ManageMemberProfileUseCase manageMemberProfileUseCase;

    @MockitoBean
    RegisterOAuthMemberUseCase registerOAuthMemberUseCase;

    @MockitoBean
    RegisterEmailMemberUseCase registerEmailMemberUseCase;

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
    @DisplayName("이메일 회원가입 성공 시 토큰과 memberId를 반환한다")
    void 이메일_회원가입_성공시_토큰과_memberId를_반환한다() throws Exception {
        given(jwtTokenProvider.parseEmailVerificationToken(any(), any())).willReturn("gildong@example.com");
        given(registerEmailMemberUseCase.register(any(EmailRegisterMemberCommand.class))).willReturn(100L);
        given(jwtTokenProvider.createAccessToken(100L, null)).willReturn("access-token");
        given(jwtTokenProvider.createRefreshToken(100L)).willReturn("refresh-token");

        mockMvc.perform(post("/api/v1/member/register/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "rawPassword": "Password123!",
                      "name": "홍길동",
                      "nickname": "길동",
                      "emailVerificationToken": "email-token",
                      "schoolId": 1,
                      "termsAgreements": [
                        {"termsId": 10, "isAgreed": true}
                      ]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.memberId").value(100L))
            .andExpect(jsonPath("$.result.accessToken").value("access-token"))
            .andExpect(jsonPath("$.result.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("이메일 회원가입 요청의 rawPassword가 blank이면 400")
    void 이메일_회원가입_요청의_rawPassword가_blank이면_400() throws Exception {
        mockMvc.perform(post("/api/v1/member/register/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "rawPassword": " ",
                      "name": "홍길동",
                      "nickname": "길동",
                      "emailVerificationToken": "email-token",
                      "schoolId": 1,
                      "termsAgreements": [
                        {"termsId": 10, "isAgreed": true}
                      ]
                    }
                    """))
            .andExpect(status().isBadRequest());

        then(registerEmailMemberUseCase).should(never()).register(any());
    }

    @Test
    @DisplayName("내 회원 정보 수정은 로그인 회원 ID로 UseCase를 호출한다")
    void 내_회원_정보_수정은_로그인_회원_ID로_UseCase를_호출한다() throws Exception {
        given(assembler.fromMemberId(TEST_MEMBER_ID)).willReturn(MemberInfoResponse.builder()
            .id(TEST_MEMBER_ID)
            .name("홍길동")
            .build());

        mockMvc.perform(patch("/api/v1/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(java.util.Map.of("profileImageId", "file-id"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.id").value(TEST_MEMBER_ID));

        then(manageMemberUseCase).should().updateMember(any(UpdateMemberCommand.class));
    }
}
