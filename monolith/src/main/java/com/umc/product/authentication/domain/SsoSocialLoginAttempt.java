package com.umc.product.authentication.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.common.BaseEntity;
import com.umc.product.common.domain.enums.OAuthProvider;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 제공자에 다녀오는 동안 Mac의 요청과 브라우저를 연결한다. UMC 코드와는 별개의 일회용 시도다. */
@Entity
@Table(name = "sso_social_login_attempt")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SsoSocialLoginAttempt extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OAuthProvider provider;
    @Column(nullable = false, unique = true, length = 64)
    private String stateHash; // 제공자에게 보낸 서버 state의 해시. callback 조회용 고유 키다.
    @Column(nullable = false, length = 64)
    private String bindingHash; // 브라우저 쿠키 확인값의 해시. state만 탈취한 다른 브라우저를 거절한다.
    @Column(nullable = false, length = 100)
    private String clientId;
    @Column(nullable = false, length = 500)
    private String redirectUri;
    @Column(nullable = false, length = 512)
    private String macState; // 앱이 생성한 원래 state. 성공·실패 복귀 때 앱에 그대로 돌려줄 값이다.
    @Column(nullable = false, length = 128)
    private String codeChallenge; // 앱 verifier의 S256 challenge. UMC SSO 코드를 발급할 때 전달한다.
    @Column(nullable = false, length = 500)
    private String callbackUri; // 소셜 제공자 코드 교환에 사용할 서버 HTTPS 주소. 앱 복귀 주소와 다르다.
    @Column(nullable = false, length = 128)
    private String nonce; // OIDC ID Token이 이번 로그인 요청에서 발급됐는지 대조하는 난수다.
    @Column(nullable = false)
    private Instant expiresAt;
    private Instant usedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private SsoSocialLoginAttempt(OAuthProvider provider, String stateHash, String bindingHash, String clientId,
                                 String redirectUri, String macState, String codeChallenge, String callbackUri,
                                 String nonce, Instant expiresAt) {
        if (provider == null || expiresAt == null || blank(stateHash) || blank(bindingHash) || blank(clientId)
            || blank(redirectUri) || blank(macState) || blank(codeChallenge) || blank(callbackUri) || blank(nonce)) {
            throw invalidAttempt();
        }
        this.provider = provider;
        this.stateHash = stateHash;
        this.bindingHash = bindingHash;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.macState = macState;
        this.codeChallenge = codeChallenge;
        this.callbackUri = callbackUri;
        this.nonce = nonce;
        this.expiresAt = expiresAt;
    }

    public static SsoSocialLoginAttempt create(OAuthProvider provider, String stateHash, String bindingHash,
                                              String clientId, String redirectUri, String macState,
                                              String codeChallenge, String callbackUri, String nonce,
                                              Instant expiresAt) {
        return SsoSocialLoginAttempt.builder().provider(provider).stateHash(stateHash).bindingHash(bindingHash)
            .clientId(clientId).redirectUri(redirectUri).macState(macState).codeChallenge(codeChallenge)
            .callbackUri(callbackUri).nonce(nonce).expiresAt(expiresAt).build();
    }

    /**
     * callback을 보낸 제공자와 브라우저가 시작 요청과 일치하고, 아직 유효한 미사용 시도인지 검사한다.
     * 만료 시각과 정확히 같은 시점도 만료로 본다. 모든 검증을 통과했을 때만 사용 시각을 기록한다.
     * 동시 요청을 직렬화하는 DB 잠금과 커밋은 application/persistence 계층이 담당한다.
     */
    public void consume(OAuthProvider requestedProvider, String requestedBindingHash, Instant now) {
        if (usedAt != null || now == null || !expiresAt.isAfter(now) || provider != requestedProvider
            || requestedBindingHash == null || !MessageDigest.isEqual(
                bindingHash.getBytes(StandardCharsets.UTF_8), requestedBindingHash.getBytes(StandardCharsets.UTF_8))) {
            throw invalidAttempt();
        }
        usedAt = now;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static AuthenticationDomainException invalidAttempt() {
        return new AuthenticationDomainException(AuthenticationErrorCode.INVALID_SSO_SOCIAL_ATTEMPT);
    }
}
