package com.umc.product.authentication.application.port.in.command.dto;

import com.umc.product.authentication.domain.MemberOAuth;
import com.umc.product.common.domain.enums.OAuthProvider;
import lombok.Builder;

@Builder
public record LinkOAuthCommand(
        Long memberId,
        OAuthProvider provider,
        String providerId
) {
    public static MemberOAuth toEntity(LinkOAuthCommand command) {
        return MemberOAuth.builder()
                .memberId(command.memberId())
                .provider(command.provider())
                .providerId(command.providerId())
                .build();
    }
}
