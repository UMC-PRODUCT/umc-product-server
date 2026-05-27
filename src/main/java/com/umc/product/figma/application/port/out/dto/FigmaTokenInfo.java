package com.umc.product.figma.application.port.out.dto;

import java.time.Instant;

public record FigmaTokenInfo(
    String accessToken,
    String refreshToken,
    Instant expiresAt,
    String scope
) {
}
