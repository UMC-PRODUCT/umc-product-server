package com.umc.product.authentication.adapter.out.external;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseCreator;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.common.domain.enums.ClientType;
import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.global.cache.application.port.in.CacheUseCase;
import com.umc.product.global.cache.domain.CacheKey;
import com.umc.product.global.cache.domain.CacheLookup;
import com.umc.product.global.cache.domain.CacheNamespace;
import com.umc.product.global.cache.domain.CacheSpec;

@DisplayName("서버 소셜 로그인 JWKS 장애 분류와 기존 로그인 호환성")
class SsoSocialJwksFailureTest {
    private static final String CLIENT_ID = "server-client";
    private static final String CALLBACK = "https://api.example.com/callback";
    private static final String NONCE = "request-nonce";
    private static final KeyPair SIGNING_KEYS = OidcTokenTestSupport.rsaKeyPair();
    private static final String APPLE_PRIVATE_KEY = applePrivateKey();

    @ParameterizedTest
    @MethodSource("failures")
    @DisplayName("서버 코드 교환 후 JWKS 5xx·timeout은 캐시 상태와 무관하게 일시 장애로 구분한다")
    void 서버_JWKS_일시_장애(OAuthProvider provider, JwksFailure failure, boolean warmCache) {
        assertFailure(provider, failure, warmCache, true, true,
            AuthenticationErrorCode.SSO_SOCIAL_PROVIDER_UNAVAILABLE);
    }

    @ParameterizedTest
    @MethodSource("failures")
    @DisplayName("기존 ID Token 로그인은 JWKS 장애에도 기존 인증 오류 응답을 유지한다")
    void 기존_로그인_호환성(OAuthProvider provider, JwksFailure failure, boolean warmCache) {
        assertFailure(provider, failure, warmCache, false, true,
            AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
    }

    @ParameterizedTest
    @EnumSource(value = JwksFailure.class, names = "INVALID_JSON", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("Kakao는 OIDC를 요청하지 않았어도 ID Token 응답의 JWKS 장애를 서버 장애로 구분한다")
    void 카카오_nonce_없는_서버_교환(JwksFailure failure) {
        assertFailure(OAuthProvider.KAKAO, failure, false, true, false,
            AuthenticationErrorCode.SSO_SOCIAL_PROVIDER_UNAVAILABLE);
    }

    @ParameterizedTest
    @EnumSource(OAuthProvider.class)
    @DisplayName("JWKS가 손상된 JSON이면 통신 장애로 바꾸지 않고 인증 실패로 유지한다")
    void 손상된_JSON_거절(OAuthProvider provider) {
        assertFailure(provider, JwksFailure.INVALID_JSON, false, true, true,
            AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
    }

    private static Stream<Arguments> failures() {
        return Stream.of(OAuthProvider.values()).flatMap(provider -> Stream.of(JwksFailure.values())
            .filter(failure -> failure != JwksFailure.INVALID_JSON)
            .flatMap(failure -> Stream.of(false, true).map(warm -> Arguments.of(provider, failure, warm))));
    }

    private void assertFailure(OAuthProvider provider, JwksFailure failure, boolean warmCache,
                               boolean serverLogin, boolean oidc, AuthenticationErrorCode expected) {
        // 실제 resolver와 verifier를 사용하고 외부 HTTP 응답만 대체한다.
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        OidcPublicKeyResolver resolver = new OidcPublicKeyResolver(
            restClient, new ObjectMapper(), new MemoryCache());
        if (warmCache) {
            server.expect(requestTo(jwksUri(provider))).andRespond(withSuccess(
                OidcTokenTestSupport.jwks("old-kid", (RSAPublicKey) SIGNING_KEYS.getPublic()),
                MediaType.APPLICATION_JSON));
        }
        String token = token(provider, "new-kid");
        if (serverLogin) {
            // 코드 교환은 성공하고, 그 다음 서명 검증용 공개키 조회에서만 장애가 발생한다.
            server.expect(requestTo(tokenUri(provider))).andRespond(withSuccess(
                "{\"id_token\":\"" + token + "\"}", MediaType.APPLICATION_JSON));
        }
        server.expect(requestTo(jwksUri(provider))).andRespond(response(failure));
        if (warmCache) {
            verify(provider, restClient, resolver, token(provider, "old-kid"), false, oidc);
        }

        assertThatThrownBy(() -> verify(provider, restClient, resolver, token, serverLogin, oidc))
            .isInstanceOf(AuthenticationDomainException.class).extracting("baseCode").isEqualTo(expected);
        server.verify();
    }

    private void verify(OAuthProvider provider, RestClient restClient, OidcPublicKeyResolver resolver,
                        String token, boolean serverLogin, boolean oidc) {
        OidcJwksCacheProperties cache = new OidcJwksCacheProperties(Duration.ofHours(1), 1L);
        switch (provider) {
            case GOOGLE -> {
                GoogleTokenVerifier verifier = new GoogleTokenVerifier(restClient, resolver,
                    new GoogleOAuthProperties(List.of(CLIENT_ID), cache));
                if (serverLogin) {
                    verifier.verifyAuthorizationCode("code", CALLBACK, CLIENT_ID, "secret", NONCE);
                } else {
                    verifier.verifyIdToken(token);
                }
            }
            case KAKAO -> {
                KakaoTokenVerifier verifier = new KakaoTokenVerifier(restClient,
                    new KakaoOAuthProperties(CLIENT_ID, "", "", List.of(CALLBACK), null, cache), resolver);
                if (serverLogin) {
                    verifier.verifyAuthorizationCode("code", CALLBACK, oidc ? NONCE : null);
                } else {
                    verifier.verifyIdToken(token);
                }
            }
            case APPLE -> {
                AppleTokenVerifier verifier = new AppleTokenVerifier(new AppleOAuthProperties(
                    "ios-client", CLIENT_ID, "team", "key", APPLE_PRIVATE_KEY, cache),
                    restClient, new ObjectMapper(), resolver);
                if (serverLogin) {
                    verifier.verifyAuthorizationCode("code", ClientType.WEB, CALLBACK, NONCE);
                } else {
                    verifier.verifyIdToken(token, CLIENT_ID);
                }
            }
        }
    }

    private String token(OAuthProvider provider, String kid) {
        String issuer = switch (provider) {
            case GOOGLE -> "https://accounts.google.com";
            case KAKAO -> "https://kauth.kakao.com";
            case APPLE -> "https://appleid.apple.com";
        };
        return OidcTokenTestSupport.signedIdToken(SIGNING_KEYS, kid, issuer, CLIENT_ID,
            "provider-member", Map.of("nonce", NONCE));
    }

    private String jwksUri(OAuthProvider provider) {
        return switch (provider) {
            case GOOGLE -> "https://www.googleapis.com/oauth2/v3/certs";
            case KAKAO -> "https://kauth.kakao.com/.well-known/jwks.json";
            case APPLE -> "https://appleid.apple.com/auth/keys";
        };
    }

    private String tokenUri(OAuthProvider provider) {
        return switch (provider) {
            case GOOGLE -> "https://oauth2.googleapis.com/token";
            case KAKAO -> "https://kauth.kakao.com/oauth/token";
            case APPLE -> "https://appleid.apple.com/auth/token";
        };
    }

    private ResponseCreator response(JwksFailure failure) {
        return switch (failure) {
            case HTTP_5XX -> withServerError();
            case REQUEST_TIMEOUT -> withException(new SocketTimeoutException());
            case BODY_TIMEOUT -> request -> brokenBody(new SocketTimeoutException());
            case BODY_DISCONNECT -> request -> brokenBody(new IOException("연결 끊김"));
            case INVALID_JSON -> withSuccess("{invalid-json", MediaType.APPLICATION_JSON);
        };
    }

    private ClientHttpResponse brokenBody(IOException failure) {
        // HTTP 상태/헤더 수신은 성공하고, JSON 본문을 읽는 중에만 IOException이 발생한다.
        MockClientHttpResponse response = new MockClientHttpResponse(new InputStream() {
            @Override
            public int read() throws IOException {
                throw failure;
            }
        }, HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return response;
    }

    private enum JwksFailure {
        HTTP_5XX, REQUEST_TIMEOUT, BODY_TIMEOUT, BODY_DISCONNECT, INVALID_JSON
    }

    private static String applePrivateKey() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(new ECGenParameterSpec("secp256r1"));
            return "-----BEGIN PRIVATE KEY-----\n" + Base64.getEncoder()
                .encodeToString(generator.generateKeyPair().getPrivate().getEncoded()) + "\n-----END PRIVATE KEY-----";
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static class MemoryCache implements CacheUseCase {
        private final Map<String, Object> values = new HashMap<>();

        @Override
        public <T> CacheLookup<T> get(CacheSpec<T> spec, CacheKey key) {
            Object value = values.get(spec.namespace().value() + ":" + key.value());
            return value == null ? new CacheLookup.Miss<>() : new CacheLookup.Hit<>(spec.valueType().cast(value));
        }

        @Override
        public <T> void put(CacheSpec<T> spec, CacheKey key, T value) {
            values.put(spec.namespace().value() + ":" + key.value(), value);
        }

        @Override
        public void evict(CacheNamespace namespace, CacheKey key) {
            values.remove(namespace.value() + ":" + key.value());
        }
    }
}
