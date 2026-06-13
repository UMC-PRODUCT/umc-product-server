package com.umc.product.global.security;

import java.time.Instant;
import java.util.UUID;

public record RefreshTokenClaims(
    Long memberId,
    UUID jti,
    Instant expiresAt
) {
}
