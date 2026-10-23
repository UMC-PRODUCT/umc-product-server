package com.umc.product.authentication.adapter.out.external;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.umc.product.authentication.domain.OAuthAttributes;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.global.cache.domain.CacheNamespace;
import com.umc.product.global.logging.ExternalApiCallLogger;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Google ID 토큰 검증 Adapter
 * <p>
 * Google OIDC ID Token은 JWKS 공개키로 로컬 검증하고, 기존 access token은 tokeninfo endpoint로 검증합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleTokenVerifier {

    private static final String GOOGLE_JWKS_URL = "https://www.googleapis.com/oauth2/v3/certs";
    private static final Set<String> GOOGLE_ISSUERS = Set.of("https://accounts.google.com", "accounts.google.com");
    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo";
    private static final String GOOGLE_REVOKE_URL = "https://oauth2.googleapis.com/revoke";

    private final RestClient restClient;
    private final OidcPublicKeyResolver publicKeyResolver;
    private final GoogleOAuthProperties googleProperties;

    /**
     * Google OAuth 토큰을 검증하고 OAuthAttributes로 변환합니다.
     *
     * @param token Google에서 발급받은 ID Token 또는 기존 Access Token
     * @return OAuthAttributes
     * @throws AuthenticationDomainException 토큰 검증 실패 시
     */
    public OAuthAttributes verify(String token) {
        if (isJwt(token)) {
            return verifyIdToken(token);
        }
        return verifyAccessToken(token);
    }

    public OAuthAttributes verifyIdToken(String idToken) {
        return verifyIdToken(idToken, null, null);
    }

    public OAuthAttributes verifyIdToken(String idToken, String expectedClientId, String expectedNonce) {
        log.debug("Google ID Token 검증 시작");

        try {
            String kid = publicKeyResolver.extractKid(idToken);
            Claims claims = Jwts.parser()
                .verifyWith(publicKeyResolver.getPublicKey(googleJwksSpec(), kid, expectedClientId != null))
                .build()
                .parseSignedClaims(idToken)
                .getPayload();

            if (!GOOGLE_ISSUERS.contains(claims.getIssuer())) {
                log.warn("Google ID 토큰 issuer 불일치: actual={}", claims.getIssuer());
                throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_OAUTH_TOKEN);
            }

            Set<String> audience = claims.getAudience();
            if (audience == null || (expectedClientId == null
                ? audience.stream().noneMatch(googleProperties.clientIdList()::contains)
                : !audience.contains(expectedClientId))) {
                log.warn("Google ID 토큰 audience 불일치: expected={}, actual={}",
                    googleProperties.clientIdList(), audience);
                throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_OAUTH_TOKEN);
            }

            if (expectedNonce != null) {
                OidcLoginClaimsValidator.validate(claims, expectedClientId, expectedNonce);
            }
            String sub = claims.getSubject();
            String email = claims.get("email", String.class);
            log.debug("Google ID Token을 검증했습니다: hasEmail={}", hasEmail(email));

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("sub", sub);
            attributes.put("email", email);
            attributes.put("name", claims.get("name", String.class));
            attributes.put("picture", claims.get("picture", String.class));

            return OAuthAttributes.of("google", attributes);

        } catch (AuthenticationDomainException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Google ID Token 검증 실패: errorClass={}", e.getClass().getSimpleName());
            throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
        }
    }

    /**
     * Google의 일회용 code를 서버 web OAuth client의 자격증명과 시작 때의 HTTPS callback으로 교환한다.
     * clientId/clientSecret은 Google 콘솔의 서버 자격증명이며 UMC의 macos-app 식별자와는 다르다.
     * 응답의 ID Token을 검증할 때 모바일 허용 목록 전체가 아닌 이 web client의 audience와 nonce를 대조한다.
     * 여기서는 Google 신원만 반환하고 UMC 토큰은 이후 기존 SSO/PKCE 흐름에서 발급한다.
     */
    public OAuthAttributes verifyAuthorizationCode(String code, String redirectUri, String clientId,
                                                   String clientSecret, String nonce) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", redirectUri);
        try {
            GoogleCodeTokenResponse response = restClient.post().uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    // 잘못된 code 등 인증 거절과 제공자 서버 장애를 나눠 앱이 적절한 재시도를 안내하게 한다.
                    if (res.getStatusCode().is5xxServerError()) {
                        throw new AuthenticationDomainException(
                            AuthenticationErrorCode.SSO_SOCIAL_PROVIDER_UNAVAILABLE);
                    }
                    throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
                }).body(GoogleCodeTokenResponse.class);
            if (response == null || !StringUtils.hasText(response.idToken())) {
                throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
            }
            return verifyIdToken(response.idToken(), clientId, nonce);
        } catch (org.springframework.web.client.RestClientException e) {
            // 외부 예외 메시지에는 응답 토큰이 포함될 수 있어 원문과 cause를 전달하지 않는다.
            throw new AuthenticationDomainException(AuthenticationErrorCode.SSO_SOCIAL_PROVIDER_UNAVAILABLE);
        }
    }

    private record GoogleCodeTokenResponse(@com.fasterxml.jackson.annotation.JsonProperty("id_token") String idToken) {
    }

    private OAuthAttributes verifyAccessToken(String accessToken) {
        log.debug("Google Access Token 검증 시작");

        try {
            GoogleAccessTokenInfoResponse response = ExternalApiCallLogger.measure(
                "GOOGLE",
                "VERIFY_ACCESS_TOKEN",
                () ->
                restClient.get()
                    .uri(GOOGLE_TOKEN_INFO_URL + "?access_token=" + accessToken)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        log.warn("Google tokeninfo 호출 실패: status={}", res.getStatusCode());
                        throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_INVALID_ACCESS_TOKEN);
                    })
                    .body(GoogleAccessTokenInfoResponse.class)
            );

            if (response == null) {
                throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
            }

            if (!googleProperties.clientIdList().contains(response.aud())) {
                log.warn("Google ID 토큰 audience 불일치: expected={}, actual={}",
                    googleProperties.clientIdList(), response.aud());
                throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_OAUTH_TOKEN);
            }

            log.debug("Google Access Token을 검증했습니다: hasEmail={}", hasEmail(response.email()));

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("sub", response.sub());
            attributes.put("email", response.email());

            return OAuthAttributes.of("google", attributes);

        } catch (AuthenticationDomainException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Google Access Token 검증 실패", e);
            throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
        }
    }

    /**
     * Google OAuth 토큰을 revoke합니다.
     *
     * @param token revoke할 토큰 (access token 또는 refresh token)
     * @see <a href="https://developers.google.com/identity/protocols/oauth2/web-server#tokenrevoke">Google Token
     * Revoke</a>
     */
    public void revokeToken(String token) {
        log.info("Google token revoke 시작");

        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("token", token);

            ExternalApiCallLogger.measure("GOOGLE", "REVOKE_TOKEN", () ->
                restClient.post()
                    .uri(GOOGLE_REVOKE_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        log.warn("Google token revoke 실패: status={}", res.getStatusCode());
                        throw new AuthenticationDomainException(
                            AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
                    })
                    .toBodilessEntity()
            );

            log.info("Google token revoke를 완료했습니다");

        } catch (AuthenticationDomainException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Google token revoke 실패", e);
            throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
        }
    }

    private boolean isJwt(String token) {
        return StringUtils.hasText(token) && token.split("\\.").length == 3;
    }

    private OidcJwksSpec googleJwksSpec() {
        return new OidcJwksSpec(
            CacheNamespace.GOOGLE_JWKS,
            GOOGLE_JWKS_URL,
            googleProperties.jwksCache().ttl(),
            googleProperties.jwksCache().maxSize()
        );
    }

    private boolean hasEmail(String email) {
        return email != null && !email.isBlank();
    }

    private record GoogleAccessTokenInfoResponse(
        String sub,
        String email,
        String aud
    ) {
    }
}
