package com.umc.product.challenger.adapter.in.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.umc.product.challenger.application.port.in.query.ListChallengerDeactivationReasonUseCase;
import com.umc.product.challenger.application.port.in.query.SearchChallengerDeactivationUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerDeactivationReasonInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerDeactivationReasonListInfo;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationReason;
import com.umc.product.global.config.JacksonConfig;
import com.umc.product.global.security.JwtTokenProvider;

@WebMvcTest(controllers = ChallengerDeactivationQueryController.class)
@Import(JacksonConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ChallengerDeactivationQueryController")
class ChallengerDeactivationQueryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    ListChallengerDeactivationReasonUseCase listChallengerDeactivationReasonUseCase;

    @MockitoBean
    SearchChallengerDeactivationUseCase searchChallengerDeactivationUseCase;

    @Test
    @DisplayName("자진 탈부와 타의 탈부 사유 목록을 조회한다")
    void listReasons() throws Exception {
        given(listChallengerDeactivationReasonUseCase.listReasons()).willReturn(
            new ChallengerDeactivationReasonListInfo(
                List.of(ChallengerDeactivationReasonInfo.from(
                    ChallengerDeactivationReason.VOLUNTARY_PERSONAL_CIRCUMSTANCES
                )),
                List.of(ChallengerDeactivationReasonInfo.from(
                    ChallengerDeactivationReason.INVOLUNTARY_OTHER
                ))
            )
        );

        mockMvc.perform(get("/api/v1/challenger/admin/deactivations/reasons"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.voluntaryReasons[0].reason")
                .value("VOLUNTARY_PERSONAL_CIRCUMSTANCES"))
            .andExpect(jsonPath("$.result.voluntaryReasons[0].displayName").value("개인 사정"))
            .andExpect(jsonPath("$.result.voluntaryReasons[0].reasonDetailRequired").value(false))
            .andExpect(jsonPath("$.result.involuntaryReasons[0].reason").value("INVOLUNTARY_OTHER"))
            .andExpect(jsonPath("$.result.involuntaryReasons[0].description").value("직접 입력"))
            .andExpect(jsonPath("$.result.involuntaryReasons[0].reasonDetailRequired").value(true));
    }
}
