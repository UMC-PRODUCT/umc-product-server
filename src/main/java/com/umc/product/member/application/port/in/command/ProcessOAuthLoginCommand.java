package com.umc.product.member.application.port.in.command;

import com.umc.product.member.domain.OAuthProvider;
import java.util.Objects;

public record ProcessOAuthLoginCommand(
        OAuthProvider provider,
        String providerId,       // OAuth Provider의 사용자 ID
        String email,
        String name,
        String nickname
) {
    public ProcessOAuthLoginCommand {
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(providerId, "providerId must not be null");
        Objects.requireNonNull(email, "email must not be null");
    }
}
