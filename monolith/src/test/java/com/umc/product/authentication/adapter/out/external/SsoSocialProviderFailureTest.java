package com.umc.product.authentication.adapter.out.external;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;

import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.common.domain.enums.ClientType;

@DisplayName("소셜 제공자 일시 장애 구분")
class SsoSocialProviderFailureTest {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Google token 서버의 5xx와 timeout은 인증 실패와 구분한다")
    void googleServerFailure(boolean timeout) {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://oauth2.googleapis.com/token")).andRespond(timeout ? withException(new java.net.SocketTimeoutException()) : withServerError());
        GoogleTokenVerifier verifier = new GoogleTokenVerifier(builder.build(), null, new GoogleOAuthProperties(List.of(), null));
        assertUnavailable(() -> verifier.verifyAuthorizationCode("code", "https://api.example.com/callback",
            "client", "secret", "nonce"));
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Kakao token 서버의 5xx와 timeout은 인증 실패와 구분한다")
    void kakaoServerFailure(boolean timeout) {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://kauth.kakao.com/oauth/token")).andRespond(timeout ? withException(new java.net.SocketTimeoutException()) : withServerError());
        String callback = "https://api.example.com/callback";
        KakaoTokenVerifier verifier = new KakaoTokenVerifier(builder.build(),
            new KakaoOAuthProperties("client", "", "", List.of(callback), null), null);
        assertUnavailable(() -> verifier.verifyAuthorizationCode("code", callback, null));
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Apple token 서버의 5xx와 timeout은 인증 실패와 구분한다")
    void appleServerFailure(boolean timeout) throws Exception {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://appleid.apple.com/auth/token")).andRespond(timeout ? withException(new java.net.SocketTimeoutException()) : withServerError());
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new java.security.spec.ECGenParameterSpec("secp256r1"));
        String pem = "-----BEGIN PRIVATE KEY-----\n" + Base64.getEncoder()
            .encodeToString(generator.generateKeyPair().getPrivate().getEncoded()) + "\n-----END PRIVATE KEY-----";
        AppleTokenVerifier verifier = new AppleTokenVerifier(new AppleOAuthProperties(
            "ios", "web", "team", "key", pem, null), builder.build(), new ObjectMapper(), null);
        assertUnavailable(() -> verifier.verifyAuthorizationCode("code", ClientType.WEB,
            "https://api.example.com/callback", "nonce"));
        server.verify();
    }

    private void assertUnavailable(org.assertj.core.api.ThrowableAssert.ThrowingCallable action) {
        assertThatThrownBy(action).isInstanceOf(AuthenticationDomainException.class)
            .extracting("baseCode").isEqualTo(AuthenticationErrorCode.SSO_SOCIAL_PROVIDER_UNAVAILABLE);
    }
}
