package com.umc.product.member.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

public final class MemberRejoinKey {

    private MemberRejoinKey() {
    }

    public static String fromVerifiedEmail(String email) {
        String normalizedEmail = normalizeVerifiedEmail(email);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(normalizedEmail.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    public static String normalizeVerifiedEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("verified email must not be blank");
        }
        String trimmedEmail = email.trim();
        int atIndex = trimmedEmail.lastIndexOf('@');
        if (atIndex < 0) {
            return trimmedEmail;
        }
        return trimmedEmail.substring(0, atIndex + 1)
            + trimmedEmail.substring(atIndex + 1).toLowerCase(Locale.ROOT);
    }
}
