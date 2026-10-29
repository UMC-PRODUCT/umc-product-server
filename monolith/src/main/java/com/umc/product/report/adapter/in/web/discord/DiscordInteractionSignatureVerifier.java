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

import lombok.extern.slf4j.Slf4j;

/**
 * Discord 상호작용 요청이 Discord에서 온 것인지 Ed25519 서명으로 확인합니다.
 * <p>
 * 서명 대상은 {@code X-Signature-Timestamp} 값 뒤에 원문 body를 그대로 이어 붙인 바이트입니다. 재전송 공격을 막으려고 타임스탬프가 현재 시각에서
 * {@link #MAX_CLOCK_SKEW} 넘게 벗어나면 거절합니다. 공개키가 없거나 형식이 잘못되었으면 서버는 그대로 띄우고 모든 요청을 거절합니다.
 */
@Slf4j
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
        if (!properties.hasPublicKey()) {
            log.warn("Discord 상호작용 공개키(app.report.discord.public-key)가 없어 모든 상호작용 요청을 거절합니다.");
        }
    }

    public boolean isValid(String signatureHex, String timestamp, byte[] body) {
        RejectReason reason = rejectReason(signatureHex, timestamp, body);
        if (reason != null) {
            // 원인을 찾을 수 있도록 거절 이유만 남깁니다. 서명·공개키·본문 내용은 남기지 않습니다.
            log.warn(
                "Discord 상호작용 서명 검증 실패: reason={}, bodyBytes={}",
                reason,
                body == null ? 0 : body.length
            );
            return false;
        }
        return true;
    }

    private RejectReason rejectReason(String signatureHex, String timestamp, byte[] body) {
        if (publicKey == null) {
            return RejectReason.PUBLIC_KEY_NOT_CONFIGURED;
        }
        if (signatureHex == null || timestamp == null) {
            return RejectReason.MISSING_HEADER;
        }
        if (body == null || body.length == 0) {
            return RejectReason.EMPTY_BODY;
        }
        if (!isFresh(timestamp)) {
            return RejectReason.STALE_TIMESTAMP;
        }
        byte[] signature = parseHex(signatureHex);
        if (signature == null || signature.length != SIGNATURE_LENGTH) {
            return RejectReason.MALFORMED_SIGNATURE;
        }
        try {
            Signature verifier = Signature.getInstance(ALGORITHM);
            verifier.initVerify(publicKey);
            verifier.update(timestamp.getBytes(StandardCharsets.UTF_8));
            verifier.update(body);
            return verifier.verify(signature) ? null : RejectReason.SIGNATURE_MISMATCH;
        } catch (GeneralSecurityException exception) {
            return RejectReason.SIGNATURE_MISMATCH;
        }
    }

    private enum RejectReason {
        PUBLIC_KEY_NOT_CONFIGURED,
        MISSING_HEADER,
        EMPTY_BODY,
        STALE_TIMESTAMP,
        MALFORMED_SIGNATURE,
        SIGNATURE_MISMATCH
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

    /**
     * 공개키를 읽습니다. 형식이 잘못되었으면 선택 기능 때문에 서버 전체가 뜨지 못하는 일이 없도록, 에러 로그만 남기고 {@code null}을 돌려줍니다.
     */
    private static PublicKey toPublicKey(String publicKeyHex) {
        byte[] rawKey = parseHex(publicKeyHex);
        if (rawKey == null || rawKey.length != PUBLIC_KEY_LENGTH) {
            log.error("app.report.discord.public-key는 32바이트 hex 문자열(64자)이어야 합니다. 모든 Discord 상호작용 요청을 거절합니다.");
            return null;
        }
        byte[] encoded = new byte[X509_ED25519_PREFIX.length + PUBLIC_KEY_LENGTH];
        System.arraycopy(X509_ED25519_PREFIX, 0, encoded, 0, X509_ED25519_PREFIX.length);
        System.arraycopy(rawKey, 0, encoded, X509_ED25519_PREFIX.length, PUBLIC_KEY_LENGTH);
        try {
            return KeyFactory.getInstance(ALGORITHM).generatePublic(new X509EncodedKeySpec(encoded));
        } catch (GeneralSecurityException exception) {
            log.error("app.report.discord.public-key를 Ed25519 공개키로 읽을 수 없어 모든 Discord 상호작용 요청을 거절합니다.");
            return null;
        }
    }
}
