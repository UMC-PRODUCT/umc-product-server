package com.umc.product.authentication.application.port.in.command.dto;

import com.umc.product.common.domain.enums.OAuthProvider;

public record LinkOAuthCommand(
        Long memberId,
        OAuthProvider provider,
        String providerId
) {
}
