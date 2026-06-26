package com.umc.product.authentication.adapter.in.web;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.umc.product.authentication.application.port.in.command.AuthorizeSsoUseCase;
import com.umc.product.authentication.application.port.in.command.dto.SsoAuthorizationRedirectInfo;
import com.umc.product.support.IntegrationTestSupport;

import jakarta.servlet.http.Cookie;

@DisplayName("SsoOAuthController authorize")
class SsoOAuthControllerAuthorizeTest extends IntegrationTestSupport {

    @MockitoBean
    AuthorizeSsoUseCase authorizeSsoUseCase;

    @Test
    @DisplayName("authorize 성공 시 raw 브라우저 로그인 토큰을 command로 전달하고 registered redirect URI로 302 응답한다")
    void authorize_성공_redirect() throws Exception {
        // given
        given(authorizeSsoUseCase.authorize(argThat(command ->
            command.clientId().equals("backoffice")
                && command.redirectUri().equals("https://backoffice.university.neordinary.com/auth/callback")
                && command.responseType().equals("code")
                && command.state().equals("state-123")
                && command.codeChallenge().equals("challenge-123")
                && command.codeChallengeMethod().equals("S256")
                && command.rawLoginToken().equals("sso-login-token")
        ))).willReturn(SsoAuthorizationRedirectInfo.of(
            "https://backoffice.university.neordinary.com/auth/callback",
            "raw-code",
            "state-123"
        ));

        // when / then
        mockMvc.perform(get("/api/v1/oauth/authorize")
                .param("client_id", "backoffice")
                .param("redirect_uri", "https://backoffice.university.neordinary.com/auth/callback")
                .param("response_type", "code")
                .param("state", "state-123")
                .param("code_challenge", "challenge-123")
                .param("code_challenge_method", "S256")
                .cookie(new Cookie("UMC_SSO_LOGIN", "sso-login-token")))
            .andExpect(status().isFound())
            .andExpect(header().string(HttpHeaders.LOCATION, containsString(
                "https://backoffice.university.neordinary.com/auth/callback"
            )))
            .andExpect(header().string(HttpHeaders.LOCATION, containsString("code=raw-code")))
            .andExpect(header().string(HttpHeaders.LOCATION, containsString("state=state-123")));
    }

    @Test
    @DisplayName("authorize 요청에서 SSO 쿠키가 없으면 401을 반환하고 UseCase를 호출하지 않는다")
    void authorize_쿠키_없음_거부() throws Exception {
        // when / then
        mockMvc.perform(get("/api/v1/oauth/authorize")
                .param("client_id", "backoffice")
                .param("redirect_uri", "https://backoffice.university.neordinary.com/auth/callback")
                .param("response_type", "code")
                .param("state", "state-123")
                .param("code_challenge", "challenge-123")
                .param("code_challenge_method", "S256"))
            .andExpect(status().isUnauthorized());

        then(authorizeSsoUseCase).shouldHaveNoInteractions();
    }
}
