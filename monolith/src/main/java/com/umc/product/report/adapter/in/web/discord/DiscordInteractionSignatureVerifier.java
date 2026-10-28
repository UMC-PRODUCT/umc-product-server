package com.umc.product.report.adapter.in.web.discord;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

import org.springframework.stereotype.Component;

/**
 * Discord 상호작용 요청이 Discord에서 온 것인지 Ed25519 서명으로 확인합니다.
 * <p>
 * 서명 대상은 {@code X-Signature-Timestamp} 값 뒤에 원문 body를 그대로 이어 붙인 바이트입니다. 재전송 공격을 막으려고 타임스탬프가 현재 시각에서
 * {@link #MAX_CLOCK_SKEW} 넘게 벗어나면 거절합니다. 공개키가 설정되지 않았으면 모든 요청을 거절합니다.
 */
@Component
public class DiscordInteractionSignatureVerifier {

    static final Duration MAX_CLOCK_SKEW = Duration.ofMinutes(5);

    private static final String ALGORITHM = "Ed25519";
    private static final int PUBLIC_KEY_LENGTH = 32;
    private static final int SIGNATURE_LENGTH = 64;
    // 32바이트 Ed25519 원시 공개키 앞에 붙이는 X.509 SubjectPublicKeyInfo 헤더입니다.
    private static final byte[] X509_ED25519_PREFIX = HexFormat.of().parseHex("302a300506032b6570032100");

    private final PublicKey publicKey;
    private final Clock clock;

    public DiscordInteractionSignatureVerifier(ReportDiscordInteractionProperties properties, Clock clock) {
        this.publicKey = properties.hasPublicKey() ? toPublicKey(properties.publicKey().trim()) : null;
        this.clock = clock;
    }

    public boolean isValid(String signatureHex, String timestamp, byte[] body) {
        if (publicKey == null || signatureHex == null || timestamp == null || body == null) {
            return false;
        }
        if (!isFresh(timestamp)) {
            return false;
        }
        byte[] signature = parseHex(signatureHex);
        if (signature == null || signature.length != SIGNATURE_LENGTH) {
            return false;
        }
        try {
            Signature verifier = Signature.getInstance(ALGORITHM);
            verifier.initVerify(publicKey);
            verifier.update(timestamp.getBytes(StandardCharsets.UTF_8));
            verifier.update(body);
            return verifier.verify(signature);
        } catch (GeneralSecurityException exception) {
            return false;
        }
    }

    private boolean isFresh(String timestamp) {
        long epochSecond;
        try {
            epochSecond = Long.parseLong(timestamp);
        } catch (NumberFormatException exception) {
            return false;
        }
        Duration skew = Duration.between(Instant.ofEpochSecond(epochSecond), Instant.now(clock)).abs();
        return skew.compareTo(MAX_CLOCK_SKEW) <= 0;
    }

    private static byte[] parseHex(String value) {
        try {
            return HexFormat.of().parseHex(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static PublicKey toPublicKey(String publicKeyHex) {
        byte[] rawKey = parseHex(publicKeyHex);
        if (rawKey == null || rawKey.length != PUBLIC_KEY_LENGTH) {
            throw new IllegalStateException("app.report.discord.public-key는 32바이트 hex 문자열이어야 합니다.");
        }
        byte[] encoded = new byte[X509_ED25519_PREFIX.length + PUBLIC_KEY_LENGTH];
        System.arraycopy(X509_ED25519_PREFIX, 0, encoded, 0, X509_ED25519_PREFIX.length);
        System.arraycopy(rawKey, 0, encoded, X509_ED25519_PREFIX.length, PUBLIC_KEY_LENGTH);
        try {
            return KeyFactory.getInstance(ALGORITHM).generatePublic(new X509EncodedKeySpec(encoded));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("app.report.discord.public-key를 Ed25519 공개키로 읽을 수 없습니다.", exception);
        }
    }
}
