package com.umc.product.report.adapter.in.web.discord;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Arrays;
import java.util.HexFormat;

/**
 * 테스트 안에서 만든 Ed25519 키로 Discord 요청 서명을 흉내 냅니다.
 */
final class DiscordInteractionTestKeys {

    private final KeyPair keyPair;

    private DiscordInteractionTestKeys(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    static DiscordInteractionTestKeys generate() {
        try {
            return new DiscordInteractionTestKeys(KeyPairGenerator.getInstance("Ed25519").generateKeyPair());
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(exception);
        }
    }

    /**
     * Discord 개발자 포털에 표시되는 형식(32바이트 원시 공개키의 hex)입니다.
     */
    String publicKeyHex() {
        byte[] encoded = keyPair.getPublic().getEncoded();
        return HexFormat.of().formatHex(Arrays.copyOfRange(encoded, encoded.length - 32, encoded.length));
    }

    String sign(String timestamp, String body) {
        try {
            Signature signer = Signature.getInstance("Ed25519");
            signer.initSign(keyPair.getPrivate());
            signer.update(timestamp.getBytes(StandardCharsets.UTF_8));
            signer.update(body.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(signer.sign());
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
