package com.umc.product.authentication.application.service;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import com.umc.product.authentication.application.port.in.command.AuthorizeSsoUseCase;
import com.umc.product.authentication.application.port.in.command.ManageSsoBrowserLoginUseCase;
import com.umc.product.authentication.application.port.in.command.ManageSsoSocialLoginUseCase;
import com.umc.product.authentication.application.port.in.command.dto.AuthorizeSsoCommand;
import com.umc.product.authentication.application.port.in.command.dto.CompleteSsoSocialLoginCommand;
import com.umc.product.authentication.application.port.in.command.dto.SsoSocialLoginStartInfo;
import com.umc.product.authentication.application.port.in.command.dto.StartSsoSocialLoginCommand;
import com.umc.product.authentication.application.port.in.dto.SsoBrowserOAuthLoginResult;
import com.umc.product.authentication.application.port.out.LoadSsoClientPort;
import com.umc.product.authentication.application.port.out.SsoSocialLoginAttemptPort;
import com.umc.product.authentication.application.port.out.SsoSocialProviderPort;
import com.umc.product.authentication.domain.SsoClient;
import com.umc.product.authentication.domain.SsoSocialLoginAttempt;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.global.client.ClientServiceType;

import lombok.RequiredArgsConstructor;

/**
 * Mac의 로그인 시작 요청부터 제공자 callback, 기존 UMC SSO 코드 발급까지 연결한다.
 * <p>
 * 제공자 code는 소셜 계정 확인에만 사용한다. 최종 Access/Refresh Token은 여기서 발급하지 않고,
 * Mac이 UMC code와 처음 보관한 PKCE verifier를 기존 token API에 보내도록 한다.
 * 제공자 HTTP 상세는 외부 Port에, 회원 조회와 로그인 증명 발급은 기존 로그인 UseCase에 위임한다.
 */
@Service
@RequiredArgsConstructor
public class SsoSocialLoginCommandService implements ManageSsoSocialLoginUseCase {
    private static final Duration ATTEMPT_TTL = Duration.ofMinutes(10);
    private final LoadSsoClientPort clients;
    private final SsoSocialLoginAttemptPort attempts;
    private final SsoSocialLoginAttemptService consumer;
    private final SsoSocialProviderPort provider;
    private final ManageSsoBrowserLoginUseCase browser;
    private final AuthorizeSsoUseCase authorize;
    private final SecureTokenGenerator tokens;
    private final PkceVerifier pkce;
    private final Clock clock;

    @Override
    @Transactional
    public SsoSocialLoginStartInfo start(StartSsoSocialLoginCommand command) {
        // 1차 구현은 macos-app 전용이다. state는 앱이 복귀 응답을 원래 요청과 대조할 때 사용한다.
        // 입력 검증을 먼저 끝내야 잘못된 요청으로 외부 로그인 화면이나 DB 시도를 만들지 않는다.
        if (command.provider() == null || !"macos-app".equals(command.clientId())
            || !"code".equals(command.responseType()) || !StringUtils.hasText(command.state())
            || command.state().length() > 512) {
            throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_SSO_AUTHORIZATION_REQUEST);
        }
        SsoClient client = clients.getByClientId(command.clientId());
        if (client.serviceType() != ClientServiceType.MACOS_APP || !client.isNativeApp() || !client.requirePkce()) {
            throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_SSO_CLIENT);
        }
        // 임의 scheme이나 URL을 허용하면 로그인 결과가 공격자 앱으로 전달될 수 있어 정확히 대조한다.
        if (!client.allowsRedirectUri(command.redirectUri())) {
            throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_SSO_REDIRECT_URI);
        }
        // verifier 자체는 앱에만 남긴다. 서버는 challenge를 저장했다가 최종 token 교환에서 검증한다.
        pkce.requireCodeChallenge(command.codeChallenge());
        pkce.requireS256(command.codeChallengeMethod());
        // Mac의 state는 앱 복귀 확인용이고, 제공자 state는 서버 callback 확인용이다.
        // 두 값을 분리하여 외부 제공자가 보낸 값으로 앱 복귀 주소나 PKCE 정보를 결정하지 않는다.
        String state = tokens.generateOpaqueToken();
        String nonce = tokens.generateOpaqueToken();
        // 같은 브라우저의 유효한 쿠키는 재사용해 여러 로그인 시도가 서로의 쿠키를 덮어쓰지 않게 한다.
        // 쿠키는 사용자 로그인 증명이 아니라, 시작한 브라우저와 callback 브라우저가 같다는 확인값이다.
        String binding = command.browserBinding();
        if (binding == null || !binding.matches("[A-Za-z0-9_-]{43}")) {
            binding = tokens.generateOpaqueToken();
        }
        SsoSocialProviderPort.Authorization authorization = provider.prepare(command.provider(), state, nonce);
        // callback에서 필요한 원본 요청은 서버에 보관하고, 외부에 전달되는 비밀 난수는 해시로 조회한다.
        attempts.save(SsoSocialLoginAttempt.create(command.provider(), tokens.sha256Hex(state),
            tokens.sha256Hex(binding), client.clientId(), command.redirectUri(), command.state(),
            command.codeChallenge(), authorization.callbackUri(), nonce, Instant.now(clock).plus(ATTEMPT_TTL)));
        return new SsoSocialLoginStartInfo(authorization.uri(), binding);
    }

    @Override
    // 제공자 응답을 기다리는 동안 DB 트랜잭션을 유지하지 않는다.
    // 시도의 일회용 소비만 별도 서비스의 REQUIRES_NEW에서 먼저 커밋한다.
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String complete(CompleteSsoSocialLoginCommand command) {
        // 이 검증은 try 밖에 둔다. 신뢰할 수 없는 callback에는 앱 redirect 자체를 제공하지 않는다.
        SsoSocialLoginAttempt attempt = consumer.consume(command.provider(), command.state(), command.browserBinding());
        // 취소·실패 응답도 신뢰할 수 있는 시도를 소비한 뒤 처리한다.
        // 제공자가 보낸 오류 설명을 URL에 그대로 노출하지 않고 앱과 합의한 오류값만 전달한다.
        if (StringUtils.hasText(command.error())) {
            // Apple 웹 로그인은 표준 access_denied 대신 전용 취소값을 보낸다.
            // Apple에서 온 경우에만 취소로 인정하며, 코드가 함께 온 모순된 응답은 실패로 유지한다.
            boolean cancelled = "access_denied".equals(command.error())
                || (command.provider() == OAuthProvider.APPLE && "user_cancelled_authorize".equals(command.error()));
            return errorRedirect(attempt, cancelled
                && !StringUtils.hasText(command.code())
                ? "access_denied" : "authentication_failed");
        }
        if (!StringUtils.hasText(command.code()) || command.code().length() > 4096) {
            return errorRedirect(attempt, "authentication_failed");
        }
        try {
            SsoSocialProviderPort.Identity identity = provider.exchange(attempt.getProvider(), command.code(),
                attempt.getCallbackUri(), attempt.getNonce());
            SsoBrowserOAuthLoginResult login = browser.loginByVerifiedOAuth(identity.attributes(),
                identity.appleRefreshToken(), identity.clientId());
            // 기존 결과 DTO의 hasLoginCookie는 내부 로그인 증명 발급 여부를 나타낸다.
            // 이 경로에서는 브라우저 로그인 쿠키를 만들지 않으며, 미연결 계정의 가입도 수행하지 않는다.
            if (!login.hasLoginCookie()) {
                return errorRedirect(attempt, "registration_required");
            }
            // 제공자 code는 여기서 끝낸다. 앱에는 기존 SSO가 새로 발급한 UMC code만 전달하고,
            // 최종 토큰은 앱이 처음 보관한 verifier로 기존 PKCE 교환 API에서 받게 한다.
            return authorize.authorize(AuthorizeSsoCommand.of(attempt.getClientId(), attempt.getRedirectUri(),
                "code", attempt.getMacState(), attempt.getCodeChallenge(), "S256", login.loginToken())).redirectUri();
        } catch (AuthenticationDomainException e) {
            return errorRedirect(attempt, e.getBaseCode() == AuthenticationErrorCode.SSO_SOCIAL_PROVIDER_UNAVAILABLE
                ? "temporarily_unavailable" : "authentication_failed");
        }
    }

    /** 저장 시 검증한 앱 주소만 사용하며, 원래 Mac state의 공백·& 등은 별도 query가 되지 않도록 인코딩한다. */
    private String errorRedirect(SsoSocialLoginAttempt attempt, String error) {
        return UriComponentsBuilder.fromUriString(attempt.getRedirectUri()).queryParam("error", error)
            .queryParam("state", UriUtils.encode(attempt.getMacState(), StandardCharsets.UTF_8))
            .build(true).toUriString();
    }
}
