package com.umc.product.global.security;

import com.umc.product.common.domain.enums.OAuthProvider;

/**
 * OAuth Verification Token의 정보를 담는 레코드
 */
public record OAuthVerificationClaims(
        String email,
        OAuthProvider provider,
        String providerId
) {
}
