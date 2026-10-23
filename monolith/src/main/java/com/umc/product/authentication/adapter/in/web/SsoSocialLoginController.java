package com.umc.product.authentication.adapter.in.web;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.authentication.adapter.in.web.dto.request.SsoSocialStartRequest;
import com.umc.product.authentication.application.port.in.command.ManageSsoSocialLoginUseCase;
import com.umc.product.authentication.application.port.in.command.dto.CompleteSsoSocialLoginCommand;
import com.umc.product.authentication.application.port.in.command.dto.SsoSocialLoginStartInfo;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.global.security.annotation.Public;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Mac의 브라우저 로그인 진입점이다. 요청 변환·쿠키·HTTP redirect만 처리하고 인증 판단은 UseCase에 맡긴다.
 * callback은 로그인 전에도 호출되어야 하므로 공개하되, 서비스에서 state와 브라우저 결속을 검증한다.
 * Google/Kakao는 GET, Apple은 form POST로 돌아오므로 제공자별 진입점을 분리한다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/sso/social")
@Tag(name = "Authentication | SSO Social", description = "기존 연결 회원의 macOS 소셜 SSO")
public class SsoSocialLoginController {
    private final ManageSsoSocialLoginUseCase socialLogin;
    private final SsoSocialCookieWriter cookies;

    @Public
    @GetMapping("/{provider}/authorize")
    @Operation(operationId = "SSO-SOCIAL-001", summary = "macOS 소셜 로그인 시작")
    public ResponseEntity<Void> start(@PathVariable String provider, @Valid SsoSocialStartRequest request,
        @CookieValue(name = SsoSocialCookieWriter.COOKIE_NAME, required = false) String binding,
        HttpServletResponse response) {
        secureResponse(response);
        // 설정된 provider URL로만 이동하며, 브라우저 확인 쿠키를 먼저 설정해 callback에서 대조한다.
        SsoSocialLoginStartInfo result = socialLogin.start(request.toCommand(parseProvider(provider), binding));
        cookies.write(response, result.browserBinding());
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(result.authorizationUri())).build();
    }

    @Public
    @GetMapping("/google/callback")
    @Operation(operationId = "SSO-SOCIAL-002", summary = "Google 서버 callback")
    public ResponseEntity<Void> googleCallback(
        @RequestParam(required = false) String code, @RequestParam(required = false) String state,
        @RequestParam(required = false) String error,
        @CookieValue(name = SsoSocialCookieWriter.COOKIE_NAME, required = false) String binding,
        HttpServletResponse response) {
        return complete(OAuthProvider.GOOGLE, code, state, error, binding, response);
    }

    @Public
    @GetMapping("/kakao/callback")
    @Operation(operationId = "SSO-SOCIAL-004", summary = "Kakao 서버 callback")
    public ResponseEntity<Void> kakaoCallback(
        @RequestParam(required = false) String code, @RequestParam(required = false) String state,
        @RequestParam(required = false) String error,
        @CookieValue(name = SsoSocialCookieWriter.COOKIE_NAME, required = false) String binding,
        HttpServletResponse response) {
        // servlet path나 전달된 provider 값 대신 매핑된 진입점에서 제공자를 확정한다.
        return complete(OAuthProvider.KAKAO, code, state, error, binding, response);
    }

    @Public
    @PostMapping(value = "/apple/callback", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Operation(operationId = "SSO-SOCIAL-003", summary = "Apple 서버 form POST callback")
    public ResponseEntity<Void> appleCallback(
        @RequestParam(required = false) String code, @RequestParam(required = false) String state,
        @RequestParam(required = false) String error,
        @CookieValue(name = SsoSocialCookieWriter.COOKIE_NAME, required = false) String binding,
        HttpServletResponse response) {
        return complete(OAuthProvider.APPLE, code, state, error, binding, response);
    }

    private ResponseEntity<Void> complete(OAuthProvider provider, String code, String state, String error,
        String binding, HttpServletResponse response) {
        secureResponse(response);
        String uri = socialLogin.complete(CompleteSsoSocialLoginCommand.of(provider, code, state, error, binding));
        // POST callback의 body가 앱 복귀 요청에 재전송되지 않도록 303으로 종료한다.
        return ResponseEntity.status(HttpStatus.SEE_OTHER).location(URI.create(uri)).build();
    }

    private OAuthProvider parseProvider(String provider) {
        return switch (provider) {
            case "google" -> OAuthProvider.GOOGLE;
            case "kakao" -> OAuthProvider.KAKAO;
            case "apple" -> OAuthProvider.APPLE;
            default -> throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_PROVIDER_NOT_FOUND);
        };
    }

    private void secureResponse(HttpServletResponse response) {
        // URL에는 일회용 code/state가 포함될 수 있으므로 응답 저장과 후속 Referer 전달을 제한한다.
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Referrer-Policy", "no-referrer");
    }
}
