package com.umc.product.authentication.application.port.in.command.dto;

import com.umc.product.common.domain.enums.OAuthProvider;
import java.util.Objects;

public record OAuthLoginCommand(
        OAuthProvider provider,
        String providerId,       // OAuth Provider의 사용자 ID
        String email,
        String name,
        String nickname
) {
    public OAuthLoginCommand {
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(providerId, "providerId must not be null");
        Objects.requireNonNull(email, "email must not be null");
    }
}
