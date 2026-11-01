package com.umc.product.authentication.adapter.out.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.authentication.domain.OAuthAttributes;
import com.umc.product.common.domain.enums.ClientType;
import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.global.cache.application.port.in.CacheUseCase;
import com.umc.product.global.cache.domain.CacheKey;
import com.umc.product.global.cache.domain.CacheLookup;
import com.umc.product.global.cache.domain.CacheNamespace;
import com.umc.product.global.cache.domain.CacheSpec;

@DisplayName("AppleTokenVerifier")
class AppleTokenVerifierTest {

    @Test
    @DisplayName("같은 Apple kid의 ID Token 검증은 JWKS를 한 번만 조회한다")
    void same_apple_kid_uses_cached_jwks() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        KeyPair keyPair = OidcTokenTestSupport.rsaKeyPair();
        server.expect(once(), requestTo("https://appleid.apple.com/auth/keys"))
            .andRespond(withSuccess(
                OidcTokenTestSupport.jwks("apple-kid", (RSAPublicKey) keyPair.getPublic()),
                MediaType.APPLICATION_JSON
            ));
        AppleTokenVerifier verifier = newVerifier(builder);
        String firstToken = appleToken(keyPair, "apple-kid", "apple-sub-1");
        String secondToken = appleToken(keyPair, "apple-kid", "apple-sub-2");

        OAuthAttributes first = verifier.verifyIdToken(firstToken, "apple-client-id");
        OAuthAttributes second = verifier.verifyIdToken(secondToken, "apple-client-id");

        assertThat(first.provider()).isEqualTo(OAuthProvider.APPLE);
        assertThat(first.providerId()).isEqualTo("apple-sub-1");
        assertThat(second.providerId()).isEqualTo("apple-sub-2");
        server.verify();
    }

    @Test
    @DisplayName("Apple 서버 로그인은 다른 시도의 nonce를 거절한다")
    void 서버_nonce_불일치() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        KeyPair keys = OidcTokenTestSupport.rsaKeyPair();
        server.expect(requestTo("https://appleid.apple.com/auth/keys"))
            .andRespond(withSuccess(OidcTokenTestSupport.jwks("apple-kid", (RSAPublicKey) keys.getPublic()),
                MediaType.APPLICATION_JSON));
        String token = OidcTokenTestSupport.signedIdToken(keys, "apple-kid", "https://appleid.apple.com",
            "apple-client-id", "apple-sub", Map.of("nonce", "other-nonce"));
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
            newVerifier(builder).verifyIdToken(token, "apple-client-id", "request-nonce"))
            .isInstanceOf(com.umc.product.authentication.domain.exception.AuthenticationDomainException.class);
    }

    @Test
    @DisplayName("서버 Apple 코드 교환은 Services ID와 callback을 보내고 nonce 검증 후 보관 정보를 반환한다")
    void 서버_코드_교환() throws Exception {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        KeyPair keys = OidcTokenTestSupport.rsaKeyPair();
        String idToken = OidcTokenTestSupport.signedIdToken(keys, "apple-kid", "https://appleid.apple.com",
            "services-id", "apple-sub", Map.of("nonce", "request-nonce"));
        server.expect(requestTo("https://appleid.apple.com/auth/token"))
            .andExpect(MockRestRequestMatchers.content().string(
                Matchers.allOf(
                    Matchers.containsString("client_id=services-id"),
                    Matchers.containsString("redirect_uri=https%3A%2F%2Fapi.example.com%2Fcallback"),
                    Matchers.containsString("code=provider-code"),
                    Matchers.containsString("client_secret="))))
            .andRespond(withSuccess(new ObjectMapper().writeValueAsString(Map.of(
                "id_token", idToken, "refresh_token", "apple-refresh")), MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://appleid.apple.com/auth/keys"))
            .andRespond(withSuccess(OidcTokenTestSupport.jwks("apple-kid", (RSAPublicKey) keys.getPublic()),
                MediaType.APPLICATION_JSON));
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        String pem = "-----BEGIN PRIVATE KEY-----\n" + Base64.getEncoder()
            .encodeToString(generator.generateKeyPair().getPrivate().getEncoded()) + "\n-----END PRIVATE KEY-----";
        AppleTokenVerifier verifier = new AppleTokenVerifier(new AppleOAuthProperties(
            "ios-id", "services-id", "team-id", "key-id", pem,
            new OidcJwksCacheProperties(Duration.ofHours(1), 1L)), builder.build(), new ObjectMapper(),
            new OidcPublicKeyResolver(builder.build(), new ObjectMapper(), new FakeCacheUseCase()));

        var result = verifier.verifyAuthorizationCode("provider-code",
            ClientType.WEB, "https://api.example.com/callback", "request-nonce");

        assertThat(result.attrs().providerId()).isEqualTo("apple-sub");
        assertThat(result.refreshToken()).isEqualTo("apple-refresh");
        assertThat(result.clientId()).isEqualTo("services-id");
        server.verify();
    }

    private AppleTokenVerifier newVerifier(RestClient.Builder builder) {
        CacheUseCase cacheUseCase = new FakeCacheUseCase();
        OidcPublicKeyResolver resolver = new OidcPublicKeyResolver(
            builder.build(),
            new ObjectMapper(),
            cacheUseCase
        );
        return new AppleTokenVerifier(
            new AppleOAuthProperties(
                "apple-client-id",
                "apple-client-id",
                "team-id",
                "key-id",
                "private-key",
                new OidcJwksCacheProperties(Duration.ofHours(1), 1L)
            ),
            builder.build(),
            new ObjectMapper(),
            resolver
        );
    }

    private String appleToken(KeyPair keyPair, String kid, String subject) {
        return OidcTokenTestSupport.signedIdToken(
            keyPair,
            kid,
            "https://appleid.apple.com",
            "apple-client-id",
            subject,
            Map.of("email", subject + "@example.com")
        );
    }

    private static class FakeCacheUseCase implements CacheUseCase {

        private final Map<String, Object> storage = new HashMap<>();

        @Override
        public <T> CacheLookup<T> get(CacheSpec<T> spec, CacheKey key) {
            Object value = storage.get(spec.namespace().value() + ":" + key.value());
            if (value == null) {
                return new CacheLookup.Miss<>();
            }
            return new CacheLookup.Hit<>(spec.valueType().cast(value));
        }

        @Override
        public <T> void put(CacheSpec<T> spec, CacheKey key, T value) {
            storage.put(spec.namespace().value() + ":" + key.value(), value);
        }

        @Override
        public void evict(CacheNamespace namespace, CacheKey key) {
            storage.remove(namespace.value() + ":" + key.value());
        }
    }
}
