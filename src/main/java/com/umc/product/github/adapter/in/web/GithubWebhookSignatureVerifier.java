package com.umc.product.github.adapter.in.web;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * GitHub 웹훅의 {@code X-Hub-Signature-256} 서명을 검증한다 (ADR-010 §4).
 *
 * <p>웹훅은 JWT 인증 없이 들어오므로, 신뢰의 근거는 GitHub App 의 webhook secret 으로 계산한 HMAC SHA-256 서명뿐이다.
 * 따라서 본 검증이 사실상의 인증 역할을 한다.
 *
 * <p>secret 이 설정되지 않은 환경에서는 모든 요청을 거부한다(fail-closed) — 검증 없이 통과시키면
 * 누구나 가짜 이벤트로 메트릭을 오염시킬 수 있다.
 */
@Component
public class GithubWebhookSignatureVerifier {

    private static final String SIGNATURE_PREFIX = "sha256=";
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final String secret;

    public GithubWebhookSignatureVerifier(@Value("${app.github.webhook.secret:}") String secret) {
        this.secret = secret;
    }

    /**
     * @param payload         서명 검증 대상 원본 바이트 (역직렬화 전의 raw body 와 동일해야 함)
     * @param signatureHeader {@code X-Hub-Signature-256} 헤더 값 (예: {@code sha256=abcd...})
     * @return 서명이 유효하면 true
     */
    public boolean isValid(byte[] payload, String signatureHeader) {
        if (payload == null || secret == null || secret.isBlank()) {
            return false;
        }
        if (signatureHeader == null || !signatureHeader.startsWith(SIGNATURE_PREFIX)) {
            return false;
        }

        String expected = SIGNATURE_PREFIX + hexHmac(payload);
        // 타이밍 공격 방지를 위해 상수 시간 비교를 사용한다.
        return MessageDigest.isEqual(
            expected.getBytes(StandardCharsets.UTF_8),
            signatureHeader.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String hexHmac(byte[] payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] digest = mac.doFinal(payload);
            // HexFormat.of() 는 소문자 16진수를 생성하며, 이는 GitHub 서명(sha256=소문자) 형식과 일치한다.
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("GitHub 웹훅 HMAC 계산에 실패했습니다.", e);
        }
    }
}
