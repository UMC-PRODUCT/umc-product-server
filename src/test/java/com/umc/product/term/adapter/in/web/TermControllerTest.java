package com.umc.product.term.adapter.in.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.umc.product.global.config.JacksonConfig;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.term.application.port.in.command.ManageTermUseCase;
import com.umc.product.term.application.port.in.query.GetRequiredTermConsentStatusUseCase;
import com.umc.product.term.application.port.in.query.GetTermUseCase;
import com.umc.product.term.application.port.in.query.dto.ActiveTermInfo;
import com.umc.product.term.domain.enums.TermType;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TermController.class)
@Import(JacksonConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("TermController")
class TermControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    GetTermUseCase getTermUseCase;

    @MockitoBean
    GetRequiredTermConsentStatusUseCase getRequiredTermConsentStatusUseCase;

    @MockitoBean
    ManageTermUseCase manageTermUseCase;

    @Test
    @DisplayName("GET /api/v1/terms 활성 약관 전체 목록을 반환한다")
    void 활성_약관_전체_목록을_반환한다() throws Exception {
        // given
        given(getTermUseCase.listActiveTerms()).willReturn(List.of(
            new ActiveTermInfo(
                1L,
                TermType.SERVICE,
                "서비스 이용약관",
                "https://example.com/terms/service",
                true,
                1L,
                Instant.parse("2026-05-26T00:00:00Z"),
                Instant.parse("2026-05-27T00:00:00Z")
            ),
            new ActiveTermInfo(
                2L,
                TermType.PRIVACY,
                "개인정보 처리방침",
                "https://example.com/terms/privacy",
                true,
                2L,
                Instant.parse("2026-05-28T00:00:00Z"),
                Instant.parse("2026-05-29T00:00:00Z")
            )
        ));

        // when & then
        mockMvc.perform(get("/api/v1/terms"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.terms").isArray())
            .andExpect(jsonPath("$.result.terms[0].id").value("1"))
            .andExpect(jsonPath("$.result.terms[0].type").value("SERVICE"))
            .andExpect(jsonPath("$.result.terms[0].typeDescription").value("서비스 이용약관"))
            .andExpect(jsonPath("$.result.terms[0].link").value("https://example.com/terms/service"))
            .andExpect(jsonPath("$.result.terms[0].isMandatory").value(true))
            .andExpect(jsonPath("$.result.terms[0].version").value("1"))
            .andExpect(jsonPath("$.result.terms[0].createdAt").value("2026-05-26T00:00:00Z"))
            .andExpect(jsonPath("$.result.terms[0].updatedAt").value("2026-05-27T00:00:00Z"))
            .andExpect(jsonPath("$.result.terms[1].type").value("PRIVACY"));
    }
}
