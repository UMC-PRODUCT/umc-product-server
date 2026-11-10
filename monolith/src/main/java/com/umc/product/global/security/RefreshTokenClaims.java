package com.umc.product.global.security;

import java.time.Instant;
import java.util.UUID;

import com.umc.product.common.domain.enums.ClientType;
import com.umc.product.global.client.ClientContextClaims;

public record RefreshTokenClaims(
    Long memberId,
    UUID jti,
    Instant expiresAt,
    ClientContextClaims clientContext,
    ClientType clientType
) {
    public RefreshTokenClaims(Long memberId, UUID jti, Instant expiresAt) {
        this(memberId, jti, expiresAt, ClientContextClaims.empty(), null);
    }

    public RefreshTokenClaims(Long memberId, UUID jti, Instant expiresAt, ClientContextClaims clientContext) {
        this(memberId, jti, expiresAt, clientContext, null);
    }

    public RefreshTokenClaims {
        clientContext = clientContext == null ? ClientContextClaims.empty() : clientContext;
    }
}
