package com.umc.product.authentication.adapter.in.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${oauth2.redirect-url:http://localhost:3000/oauth/callback}")
    private String redirectUrl;

    // TODO: 전체 다시

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        log.error("=== OAuth2 Authentication Failed ===");
        log.error("Request URI: {}", request.getRequestURI());
        log.error("Exception Type: {}", exception.getClass().getName());
        log.error("Exception Message: {}", exception.getMessage());

        if (exception instanceof OAuth2AuthenticationException oauthEx) {
            log.error("OAuth2 Error Code: {}", oauthEx.getError().getErrorCode());
            log.error("OAuth2 Error Description: {}", oauthEx.getError().getDescription());
        }

        if (exception.getCause() != null) {
            log.error("Cause Type: {}", exception.getCause().getClass().getName());
            log.error("Cause Message: {}", exception.getCause().getMessage());
        }

        // 전체 스택트레이스 출력
        log.error("Full Stack Trace:", exception);

        // 프론트엔드로 에러와 함께 리다이렉트
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
                .queryParam("error", "oauth_failed")
                .queryParam("message", exception.getMessage())
                .build()
                .toUriString();

        log.error("Redirecting to: {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
