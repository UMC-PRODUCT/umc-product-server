package com.umc.product.challenger.adapter.in.web.v2;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.umc.product.challenger.adapter.in.web.assembler.ChallengerResponseAssembler;
import com.umc.product.challenger.adapter.in.web.dto.response.ChallengerInfoResponse;
import com.umc.product.global.config.JacksonConfig;
import com.umc.product.global.security.JwtTokenProvider;

@WebMvcTest(controllers = ChallengerAdminQueryV2Controller.class)
@Import(JacksonConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ChallengerAdminQueryV2Controller")
class ChallengerAdminQueryV2ControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    ChallengerResponseAssembler assembler;

    @Test
    @DisplayName("관리자용 챌린저 상세 정보를 조회한다")
    void getAdminChallengerDetail() throws Exception {
        given(assembler.fromChallengerIdWithPointsLatestFirst(100L)).willReturn(
            ChallengerInfoResponse.builder()
                .challengerId(100L)
                .memberId(1L)
                .build()
        );

        mockMvc.perform(get("/api/v2/challenger/admin/{challengerId}", 100L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.challengerId").value(100L));
    }
}
