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
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("verified email must not be blank");
        }
        String normalizedEmail = normalizeDomain(email.trim());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(normalizedEmail.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private static String normalizeDomain(String email) {
        int atIndex = email.lastIndexOf('@');
        if (atIndex < 0) {
            return email;
        }
        return email.substring(0, atIndex + 1)
            + email.substring(atIndex + 1).toLowerCase(Locale.ROOT);
    }
}
