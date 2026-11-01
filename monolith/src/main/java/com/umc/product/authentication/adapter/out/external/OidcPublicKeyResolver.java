package com.umc.product.authentication.adapter.out.external;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.global.cache.application.port.in.CacheUseCase;
import com.umc.product.global.cache.domain.CacheKey;
import com.umc.product.global.cache.domain.CacheLookup;
import com.umc.product.global.cache.domain.CacheSpec;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OidcPublicKeyResolver {

    private static final CacheKey PUBLIC_KEYS_CACHE_KEY = CacheKey.from("public-keys");

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final CacheUseCase cacheUseCase;

    public String extractKid(String idToken) {
        try {
            String header = idToken.split("\\.")[0];
            byte[] decoded = Base64.getUrlDecoder().decode(header);
            JsonNode kid = objectMapper.readTree(new String(decoded, StandardCharsets.UTF_8)).path("kid");
            if (!kid.isTextual() || kid.asText().isBlank()) {
                throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_OAUTH_TOKEN);
            }
            return kid.asText();
        } catch (AuthenticationDomainException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_OAUTH_TOKEN);
        }
    }

    public PublicKey getPublicKey(OidcJwksSpec spec, String kid) {
        return getPublicKey(spec, kid, false);
    }

    /**
     * 서버 소셜 로그인에서만 JWKS 통신 장애를 인증 거절과 구분한다.
     * 기존 진입점은 false로 위임하여 모바일 로그인과 공개키 조회의 기존 오류 계약을 유지한다.
     * 캐시·키 검증 규칙은 공유하고, 5xx와 연결/읽기 장애의 분류만 달리한다.
     */
    public PublicKey getPublicKey(OidcJwksSpec spec, String kid, boolean serverLogin) {
        CacheSpec<OidcPublicKeys> cacheSpec = cacheSpec(spec);
        PublicKey cached = findCachedPublicKey(cacheSpec, kid);
        if (cached != null) {
            return cached;
        }

        synchronized (spec.namespace()) {
            cached = findCachedPublicKey(cacheSpec, kid);
            if (cached != null) {
                return cached;
            }

            OidcPublicKeys refreshed = fetchPublicKeys(spec, serverLogin);
            cacheUseCase.put(cacheSpec, PUBLIC_KEYS_CACHE_KEY, refreshed);
            PublicKey publicKey = refreshed.get(kid);
            if (publicKey == null) {
                log.error("OIDC JWKS에서 kid={}에 매칭되는 키를 찾을 수 없음: namespace={}", kid, spec.namespace());
                throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_OAUTH_TOKEN);
            }
            return publicKey;
        }
    }

    private PublicKey findCachedPublicKey(CacheSpec<OidcPublicKeys> cacheSpec, String kid) {
        CacheLookup<OidcPublicKeys> lookup = cacheUseCase.get(cacheSpec, PUBLIC_KEYS_CACHE_KEY);
        if (lookup instanceof CacheLookup.Hit<OidcPublicKeys> hit) {
            return hit.value().get(kid);
        }
        return null;
    }

    private OidcPublicKeys fetchPublicKeys(OidcJwksSpec spec, boolean serverLogin) {
        OidcJwksResponse jwks;
        try {
            jwks = restClient.get()
                .uri(spec.jwksUri())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.error("OIDC JWKS 조회 실패: namespace={}, status={}", spec.namespace(), res.getStatusCode());
                    if (serverLogin && res.getStatusCode().is5xxServerError()) {
                        throw new AuthenticationDomainException(
                            AuthenticationErrorCode.SSO_SOCIAL_PROVIDER_UNAVAILABLE);
                    }
                    throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
                })
                .body(OidcJwksResponse.class);
        } catch (RestClientException e) {
            if (serverLogin && isCommunicationFailure(e)) {
                // 네트워크 장애 원문/cause를 전달하지 않는다. 이후 callback에서 고정된 앱 오류값으로 변환한다.
                throw new AuthenticationDomainException(AuthenticationErrorCode.SSO_SOCIAL_PROVIDER_UNAVAILABLE);
            }
            throw e;
        }

        if (jwks == null || jwks.keys() == null || jwks.keys().isEmpty()) {
            throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
        }

        Map<String, PublicKey> keys = jwks.keys().stream()
            .filter(key -> key.kid() != null && !key.kid().isBlank())
            .filter(key -> "RSA".equalsIgnoreCase(key.kty()))
            .map(this::buildPublicKeyEntry)
            .flatMap(Optional::stream)
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue, (left, right) -> left));
        return new OidcPublicKeys(keys);
    }

    private boolean isCommunicationFailure(RestClientException exception) {
        // 응답 본문을 읽는 중 발생한 timeout/연결 끊김은 일반 RestClientException으로 감싸질 수 있다.
        // JSON 파싱 예외도 IOException의 하위 타입이므로 제외하여 잘못된 JWKS 응답과 통신 장애를 구분한다.
        Throwable cause = exception.getMostSpecificCause();
        return exception instanceof ResourceAccessException
            || (cause instanceof IOException && !(cause instanceof JsonProcessingException));
    }

    private Optional<Map.Entry<String, PublicKey>> buildPublicKeyEntry(OidcJwk jwk) {
        try {
            return Optional.of(Map.entry(jwk.kid(), buildRsaPublicKey(jwk)));
        } catch (Exception e) {
            log.warn("OIDC RSA 공개키 생성 실패, 해당 키를 건너뜁니다: kid={}", jwk.kid(), e);
            return Optional.empty();
        }
    }

    private PublicKey buildRsaPublicKey(OidcJwk jwk) throws Exception {
        byte[] nBytes = Base64.getUrlDecoder().decode(jwk.n());
        byte[] eBytes = Base64.getUrlDecoder().decode(jwk.e());

        RSAPublicKeySpec spec = new RSAPublicKeySpec(
            new BigInteger(1, nBytes),
            new BigInteger(1, eBytes)
        );
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private CacheSpec<OidcPublicKeys> cacheSpec(OidcJwksSpec spec) {
        return CacheSpec.of(spec.namespace(), OidcPublicKeys.class, spec.ttl(), spec.maxSize());
    }

    private record OidcJwksResponse(List<OidcJwk> keys) {
    }

    private record OidcJwk(
        String kty,
        String kid,
        String use,
        String alg,
        String n,
        String e
    ) {
    }

    private record OidcPublicKeys(Map<String, PublicKey> keys) {

        PublicKey get(String kid) {
            return keys.get(kid);
        }
    }
}
