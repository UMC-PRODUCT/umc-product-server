package com.umc.product.authentication.adapter.in.oauth;

import com.umc.product.authentication.domain.enums.OAuth2ResultCode;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.common.domain.enums.OAuthProvider;
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
        // OAuth 인증 정보 추출 (request attribute에서)
        String email = (String) request.getAttribute("oauth_email");
        OAuthProvider provider = (OAuthProvider) request.getAttribute("oauth_provider");
        String providerId = (String) request.getAttribute("oauth_provider_id");

        if (email == null || provider == null || providerId == null) {
            log.error("OAuth 정보가 존재하지 않습니다.");

            OAuth2ResultCode oAuth2ResultCode = OAuth2ResultCode.INFO_MISSING;

            // 무언가 잘못된 것에 대한 에러 응답 처리
            String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
                    .queryParam("success", oAuth2ResultCode.isSuccess())
                    .queryParam("code", oAuth2ResultCode.getCode())
                    .build()
                    .toUriString();
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            return;
        }

        log.info("OAuth 인증을 완료하였으나 기존 회원이 아니여서 oAuthVerificationToken을 발급합니다.");
        // 회원가입용 임시 토큰 생성
        String oAuthVerificationToken = jwtTokenProvider.createOAuthVerificationToken(email, provider, providerId);

        OAuth2ResultCode oAuth2ResultCode = OAuth2ResultCode.REGISTER_REQUIRED;

        // 프론트엔드로 리다이렉트 (회원가입 필요 상태)
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
                .queryParam("success", oAuth2ResultCode.isSuccess())
                .queryParam("code", oAuth2ResultCode.getCode())
                .queryParam("email", email)
                .queryParam("oAuthVerificationToken", oAuthVerificationToken)
                .build()
                .toUriString();

        log.info("Redirecting to registration flow: {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
