package com.umc.product.global.security;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.support.IntegrationTestSupport;

@DisplayName("로그인이 필요한 API의 비로그인 요청 응답")
class CurrentMemberAuthenticationIntegrationTest extends IntegrationTestSupport {

    @ParameterizedTest
    @ValueSource(strings = {
        "/api/v1/projects/applications?projectIds=1",
        "/api/v1/projects/permissions?ids=1",
        "/api/v1/projects/members?projectIds=1"
    })
    @DisplayName("토큰 없이 요청하면 401 인증 정보 없음으로 응답한다")
    void 토큰_없는_요청은_401로_응답한다(String uri) throws Exception {
        mockMvc.perform(get(uri))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("SECURITY-0001"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/api/v1/projects/applications?projectIds=1",
        "/api/v1/projects/permissions?ids=1",
        "/api/v1/projects/members?projectIds=1"
    })
    @DisplayName("만료된 토큰으로 요청하면 토큰을 갱신할 수 있게 401 로그인 만료로 응답한다")
    void 만료_토큰_요청은_401_만료로_응답한다(String uri) throws Exception {
        given(jwtTokenProvider.validateAccessToken(anyString()))
            .willThrow(new AuthenticationDomainException(AuthenticationErrorCode.EXPIRED_JWT_TOKEN));

        mockMvc.perform(get(uri).header("Authorization", "Bearer expired-token"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("JWT-0002"));
    }
}
