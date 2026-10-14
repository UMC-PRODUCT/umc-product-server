package com.umc.product.authentication.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.UriComponentsBuilder;

import com.umc.product.authentication.application.port.out.SsoSocialProviderPort;
import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.support.IntegrationTestSupport;

import jakarta.servlet.http.Cookie;

@DisplayName("소셜 SSO HTTP·쿠키·Security 통합")
@org.springframework.context.annotation.Import(SsoSocialLoginController.class)
class SsoSocialLoginControllerTest extends IntegrationTestSupport {
    @MockitoBean
    SsoSocialProviderPort provider;

    @Test
    @DisplayName("시작은 보안 쿠키를 발급하고 같은 브라우저의 취소는 Mac으로 복귀한다")
    void 시작과_취소_복귀() throws Exception {
        MvcResult start = start(OAuthProvider.GOOGLE);
        String cookie = start.getResponse().getHeader("Set-Cookie");
        assertThat(cookie).contains("__Host-UMC_SOCIAL_BINDING=", "Secure", "HttpOnly", "SameSite=None", "Path=/")
            .doesNotContain("Domain=");
        String state = state(start);
        Cookie binding = binding(start);
        mockMvc.perform(get("/api/v1/auth/sso/social/google/callback").cookie(binding)
                .param("state", state).param("error", "access_denied"))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", "umc-macos://auth/callback?error=access_denied&state=mac-state"))
            .andExpect(header().string("Cache-Control", "no-store"));
        mockMvc.perform(get("/api/v1/auth/sso/social/google/callback").cookie(binding)
                .param("state", state).param("error", "access_denied"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("state가 있어도 시작 브라우저 쿠키가 없으면 앱으로 보내지 않는다")
    void 쿠키_없는_callback() throws Exception {
        MvcResult start = start(OAuthProvider.GOOGLE);
        mockMvc.perform(get("/api/v1/auth/sso/social/google/callback")
                .param("state", state(start)).param("error", "access_denied"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().doesNotExist("Location"));
    }

    @Test
    @DisplayName("Apple origin의 form POST callback은 실제 보안 체인을 통과한다")
    void apple_form_post() throws Exception {
        MvcResult start = start(OAuthProvider.APPLE);
        mockMvc.perform(post("/api/v1/auth/sso/social/apple/callback")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).header("Origin", "https://appleid.apple.com")
                .cookie(binding(start)).param("state", state(start)).param("error", "user_cancelled_authorize"))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", "umc-macos://auth/callback?error=access_denied&state=mac-state"));
    }

    @Test
    @DisplayName("Apple callback의 다른 origin은 거절한다")
    void apple_다른_origin() throws Exception {
        mockMvc.perform(post("/api/v1/auth/sso/social/apple/callback")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).header("Origin", "https://evil.example.com")
                .param("state", "untrusted").param("error", "access_denied"))
            .andExpect(status().isForbidden());
    }

    private MvcResult start(OAuthProvider oauthProvider) throws Exception {
        String name = oauthProvider.name().toLowerCase(java.util.Locale.ROOT);
        given(provider.prepare(eq(oauthProvider), anyString(), anyString())).willAnswer(invocation ->
            new SsoSocialProviderPort.Authorization("https://provider.example.com/authorize?state="
                + invocation.getArgument(1, String.class), "https://api.example.com/api/v1/auth/sso/social/" + name + "/callback"));
        return mockMvc.perform(get("/api/v1/auth/sso/social/" + name + "/authorize")
                .param("client_id", "macos-app").param("redirect_uri", "umc-macos://auth/callback")
                .param("response_type", "code").param("state", "mac-state")
                .param("code_challenge", "A".repeat(43)).param("code_challenge_method", "S256"))
            .andExpect(status().isFound()).andExpect(header().string("Referrer-Policy", "no-referrer"))
            .andReturn();
    }

    private String state(MvcResult start) {
        return UriComponentsBuilder.fromUri(URI.create(start.getResponse().getHeader("Location")))
            .build().getQueryParams().getFirst("state");
    }

    private Cookie binding(MvcResult start) {
        String value = start.getResponse().getHeader("Set-Cookie").split(";", 2)[0].split("=", 2)[1];
        return new Cookie("__Host-UMC_SOCIAL_BINDING", value);
    }
}
