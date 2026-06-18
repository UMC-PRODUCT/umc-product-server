package com.umc.product.authentication.adapter.in.web.dto.response;

import com.umc.product.authentication.application.port.in.command.dto.LocalLoginResult;
import lombok.Builder;

@Builder
public record LocalLoginResponse(
    Long memberId,
    String accessToken,
    String refreshToken
) {
    public static LocalLoginResponse from(LocalLoginResult result) {
        return LocalLoginResponse.builder()
            .memberId(result.memberId())
            .accessToken(result.accessToken())
            .refreshToken(result.refreshToken())
            .build();
    }
}
