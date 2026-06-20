package com.umc.product.github.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GithubWebhookSignatureVerifierTest {

    // GitHub 공식 문서의 HMAC SHA-256 예시 벡터.
    private static final String SECRET = "It's a Secret to Everybody";
    private static final byte[] PAYLOAD = "Hello, World!".getBytes(StandardCharsets.UTF_8);
    private static final String VALID_SIGNATURE =
        "sha256=757107ea0eb2509fc211221cce984b8a37570b6d7586c22c46f4379c8b043e17";

    @Test
    @DisplayName("유효한 서명이면 true 를 반환한다")
    void 유효한_서명_검증_성공() {
        GithubWebhookSignatureVerifier verifier = new GithubWebhookSignatureVerifier(SECRET);

        assertThat(verifier.isValid(PAYLOAD, VALID_SIGNATURE)).isTrue();
    }

    @Test
    @DisplayName("서명이 변조되면 false 를 반환한다")
    void 변조된_서명_거부() {
        GithubWebhookSignatureVerifier verifier = new GithubWebhookSignatureVerifier(SECRET);

        assertThat(verifier.isValid(PAYLOAD, "sha256=deadbeefdeadbeef")).isFalse();
    }

    @Test
    @DisplayName("서명 헤더가 없으면 false 를 반환한다")
    void 서명_헤더_없음_거부() {
        GithubWebhookSignatureVerifier verifier = new GithubWebhookSignatureVerifier(SECRET);

        assertThat(verifier.isValid(PAYLOAD, null)).isFalse();
    }

    @Test
    @DisplayName("sha256= 접두사가 없으면 false 를 반환한다")
    void 잘못된_접두사_거부() {
        GithubWebhookSignatureVerifier verifier = new GithubWebhookSignatureVerifier(SECRET);

        assertThat(verifier.isValid(PAYLOAD, "md5=757107ea")).isFalse();
    }

    @Test
    @DisplayName("secret 이 설정되지 않으면 올바른 서명도 거부한다(fail-closed)")
    void secret_미설정_거부() {
        GithubWebhookSignatureVerifier verifier = new GithubWebhookSignatureVerifier("");

        assertThat(verifier.isValid(PAYLOAD, VALID_SIGNATURE)).isFalse();
    }

    @Test
    @DisplayName("payload 가 null 이면 NPE 없이 false 를 반환한다")
    void payload_null_거부() {
        GithubWebhookSignatureVerifier verifier = new GithubWebhookSignatureVerifier(SECRET);

        assertThat(verifier.isValid(null, VALID_SIGNATURE)).isFalse();
    }
}
