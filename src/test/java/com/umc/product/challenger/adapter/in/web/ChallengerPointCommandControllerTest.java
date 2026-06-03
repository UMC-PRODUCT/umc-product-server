package com.umc.product.challenger.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.umc.product.challenger.adapter.in.web.assembler.ChallengerResponseAssembler;
import com.umc.product.challenger.adapter.in.web.dto.response.ChallengerInfoResponse;
import com.umc.product.challenger.application.port.in.command.ManageChallengerUseCase;
import com.umc.product.challenger.application.port.in.command.dto.GrantChallengerPointCommand;
import com.umc.product.global.config.JacksonConfig;
import com.umc.product.global.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ChallengerPointCommandController.class)
@Import(JacksonConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ChallengerPointCommandController")
class ChallengerPointCommandControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    ManageChallengerUseCase manageChallengerUseCase;

    @MockitoBean
    ChallengerResponseAssembler assembler;

    @Test
    @DisplayName("상벌점 부여 성공 시 챌린저 정보를 반환한다")
    void 상벌점_부여_성공시_챌린저_정보를_반환한다() throws Exception {
        given(assembler.fromChallengerId(100L)).willReturn(ChallengerInfoResponse.builder()
            .challengerId(100L)
            .totalPoints(1.0)
            .build());

        mockMvc.perform(post("/api/v1/challenger/{challengerId}/points", 100L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"pointType":"CUSTOM","pointValue":1,"description":"조정"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.challengerId").value(100L));
    }

    @Test
    @DisplayName("상벌점 부여 요청의 pointType이 없으면 400")
    void 상벌점_부여_요청의_pointType이_없으면_400() throws Exception {
        mockMvc.perform(post("/api/v1/challenger/{challengerId}/points", 100L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"pointValue":1,"description":"조정"}
                    """))
            .andExpect(status().isBadRequest());

        then(manageChallengerUseCase).should(never()).grantChallengerPoint(any(GrantChallengerPointCommand.class));
    }

    @Test
    @DisplayName("상벌점 설명을 수정한다")
    void 상벌점_설명을_수정한다() throws Exception {
        mockMvc.perform(patch("/api/v1/challenger/points/{challengerPointId}", 10L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"newDescription":"수정"}
                    """))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("상벌점을 삭제한다")
    void 상벌점을_삭제한다() throws Exception {
        mockMvc.perform(delete("/api/v1/challenger/points/{challengerPointId}", 10L))
            .andExpect(status().isOk());
    }
}
