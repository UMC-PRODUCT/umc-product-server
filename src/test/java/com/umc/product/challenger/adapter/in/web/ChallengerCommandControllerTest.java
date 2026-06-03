package com.umc.product.challenger.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.umc.product.challenger.adapter.in.web.assembler.ChallengerResponseAssembler;
import com.umc.product.challenger.adapter.in.web.dto.response.ChallengerInfoResponse;
import com.umc.product.challenger.application.port.in.command.ManageChallengerUseCase;
import com.umc.product.challenger.application.port.in.command.dto.CreateChallengerCommand;
import com.umc.product.challenger.application.port.in.command.dto.UpdateChallengerCommand;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.config.JacksonConfig;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.MemberPrincipal;
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

@WebMvcTest(controllers = ChallengerCommandController.class)
@Import(JacksonConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ChallengerCommandController")
class ChallengerCommandControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    ManageChallengerUseCase manageChallengerUseCase;

    @MockitoBean
    ChallengerResponseAssembler assembler;

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
    @DisplayName("챌린저 생성 성공 시 생성된 챌린저 정보를 반환한다")
    void 챌린저_생성_성공시_생성된_챌린저_정보를_반환한다() throws Exception {
        given(manageChallengerUseCase.createChallenger(any(CreateChallengerCommand.class))).willReturn(100L);
        given(assembler.fromChallengerId(100L)).willReturn(ChallengerInfoResponse.builder()
            .challengerId(100L)
            .memberId(1L)
            .part(ChallengerPart.SPRINGBOOT)
            .build());

        mockMvc.perform(post("/api/v1/challenger")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"memberId":1,"part":"SPRINGBOOT","gisuId":9}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.challengerId").value(100L));
    }

    @Test
    @DisplayName("챌린저 생성 요청의 part가 없으면 400")
    void 챌린저_생성_요청의_part가_없으면_400() throws Exception {
        mockMvc.perform(post("/api/v1/challenger")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"memberId":1,"gisuId":9}
                    """))
            .andExpect(status().isBadRequest());

        then(manageChallengerUseCase).should(never()).createChallenger(any());
    }

    @Test
    @DisplayName("챌린저 batch 생성 요청 내부 항목의 part가 없으면 400")
    void 챌린저_batch_생성_요청_내부_항목의_part가_없으면_400() throws Exception {
        mockMvc.perform(post("/api/v1/challenger/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    [{"memberId":1,"gisuId":9}]
                    """))
            .andExpect(status().isBadRequest());

        then(manageChallengerUseCase).should(never()).createChallenger(any());
    }

    @Test
    @DisplayName("파트 변경 요청의 newPart가 없으면 400")
    void 파트_변경_요청의_newPart가_없으면_400() throws Exception {
        mockMvc.perform(patch("/api/v1/challenger/{challengerId}/part", 100L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());

        then(manageChallengerUseCase).should(never()).updateChallenger(any(UpdateChallengerCommand.class));
    }
}
