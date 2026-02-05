package com.umc.product.authentication.adapter.in.web.dto.response;

import com.umc.product.authentication.application.port.in.command.dto.NewTokens;
import lombok.Builder;

@Builder
public record RenewAccessTokenResponse(
    String accessToken,
    String refreshToken
) {
    public static RenewAccessTokenResponse from(NewTokens newTokens) {
        return RenewAccessTokenResponse.builder()
            .accessToken(newTokens.accessToken())
            .refreshToken(newTokens.refreshToken())
            .build();
    }
}
