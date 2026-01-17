package com.umc.product.authentication.adapter.in.oauth;

import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.global.security.JwtTokenProvider;
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

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${oauth2.redirect-url:http://localhost:3000/oauth/callback}")
    private String redirectUrl;

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

        // 미가입 회원인 경우 (OAuth 인증 성공, 하지만 우리 서비스 멤버 아님)
        if (exception.getCause() instanceof AuthenticationDomainException authenticationDomainException) {
            handleMemberNotFound(request, response, authenticationDomainException);
            return;
        }

        // 일반 OAuth 실패
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
                .queryParam("error", "oauth_failed")
                .queryParam("message", exception.getMessage())
                .build()
                .toUriString();

        log.error("Redirecting to: {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private void handleMemberNotFound(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationDomainException memberException
    ) throws IOException {
        log.info("OAuth authenticated but member not found. Creating registration token.");

        // OAuth 인증 정보 추출 (request attribute에서)
        String email = (String) request.getAttribute("oauth_email");
        String provider = (String) request.getAttribute("oauth_provider");

        if (email == null || provider == null) {
            log.error("OAuth info not found in request attributes");
            // fallback: 일반 에러 처리
            String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
                    .queryParam("error", "member_not_found")
                    .queryParam("message", memberException.getMessage())
                    .build()
                    .toUriString();
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            return;
        }

        // 회원가입용 임시 토큰 생성
        String registrationToken = jwtTokenProvider.createOAuthVerificationToken(email, provider);

        // 프론트엔드로 리다이렉트 (회원가입 필요 상태)
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
                .queryParam("status", "registration_required")
                .queryParam("registrationToken", registrationToken)
                .build()
                .toUriString();

        log.info("Redirecting to registration flow: {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
