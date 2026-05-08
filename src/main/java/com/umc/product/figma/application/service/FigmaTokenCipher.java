package com.umc.product.figma.application.service;

import com.umc.product.figma.adapter.out.external.FigmaOAuthProperties;
import com.umc.product.figma.domain.exception.FigmaDomainException;
import com.umc.product.figma.domain.exception.FigmaErrorCode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

/**
 * Figma refresh/access token을 평문으로 DB에 보관하지 않기 위한 대칭키 암호화 유틸. application property 기반의 키를 SHA-256으로 정규화 후 AES-GCM 으로
 * 암복호화한다. 운영 정책 결정 후 KMS / Jasypt 로 보강할 수 있도록 인터페이스를 단순하게 유지한다.
 */
@Component
public class FigmaTokenCipher {

    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int IV_LENGTH_BYTES = 12;

    private final SecretKeySpec keySpec;
    private final SecureRandom secureRandom;

    public FigmaTokenCipher(FigmaOAuthProperties properties) {
        try {
            String rawKey = properties.tokenEncryptionKey();
            if (rawKey == null || rawKey.isBlank()) {
                throw new FigmaDomainException(FigmaErrorCode.TOKEN_ENCRYPTION_FAILED,
                    "FIGMA_TOKEN_ENCRYPTION_KEY 환경변수가 설정되지 않았습니다. 애플리케이션을 시작할 수 없습니다.");
            }
            byte[] hashed = MessageDigest.getInstance("SHA-256")
                .digest(rawKey.getBytes(StandardCharsets.UTF_8));
            this.keySpec = new SecretKeySpec(hashed, "AES");
            this.secureRandom = new SecureRandom();
        } catch (Exception e) {
            throw new FigmaDomainException(FigmaErrorCode.TOKEN_ENCRYPTION_FAILED, e.getMessage());
        }
    }

    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] payload = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(payload);
        } catch (Exception e) {
            throw new FigmaDomainException(FigmaErrorCode.TOKEN_ENCRYPTION_FAILED, e.getMessage());
        }
    }

    public String decrypt(String ciphertext) {
        try {
            byte[] payload = Base64.getDecoder().decode(ciphertext);
            byte[] iv = new byte[IV_LENGTH_BYTES];
            byte[] body = new byte[payload.length - IV_LENGTH_BYTES];
            System.arraycopy(payload, 0, iv, 0, IV_LENGTH_BYTES);
            System.arraycopy(payload, IV_LENGTH_BYTES, body, 0, body.length);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] decrypted = cipher.doFinal(body);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new FigmaDomainException(FigmaErrorCode.TOKEN_ENCRYPTION_FAILED, e.getMessage());
        }
    }
}
