package com.umc.product.global.config;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.support.IntegrationTestSupport;

@DisplayName("SecurityConfig 통합 테스트")
class SecurityConfigIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("docs 진입 경로는 Scalar HTML로 리다이렉트한다")
    void docsEntryRedirectsToScalar() throws Exception {
        mockMvc.perform(get("/docs"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/docs/scalar.html"));
    }

    @Test
    @DisplayName("인증된 요청이어도 Swagger UI 경로는 접근할 수 없다")
    void authenticatedRequestCannotAccessSwaggerUi() throws Exception {
        String token = "swagger-block-token";
        given(jwtTokenProvider.validateAccessToken(token)).willReturn(true);
        given(jwtTokenProvider.parseAccessToken(token)).willReturn(1L);
        given(jwtTokenProvider.getRolesFromAccessToken(token)).willReturn(List.of("USER"));
        given(jwtTokenProvider.getClientTypeFromAccessToken(token)).willReturn(null);

        mockMvc.perform(get("/swagger-ui/index.html")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("인증된 요청이어도 기존 OpenAPI JSON 경로는 접근할 수 없다")
    void authenticatedRequestCannotAccessDefaultOpenApiJson() throws Exception {
        String token = "swagger-api-docs-block-token";
        given(jwtTokenProvider.validateAccessToken(token)).willReturn(true);
        given(jwtTokenProvider.parseAccessToken(token)).willReturn(1L);
        given(jwtTokenProvider.getRolesFromAccessToken(token)).willReturn(List.of("USER"));
        given(jwtTokenProvider.getClientTypeFromAccessToken(token)).willReturn(null);

        mockMvc.perform(get("/v3/api-docs")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }
}
