package com.umc.product.authentication.adapter.in.web.dto.response;

import com.umc.product.authentication.application.port.in.command.dto.IdPwLoginResult;
import lombok.Builder;

@Builder
public record IdPwLoginResponse(
    Long memberId,
    String accessToken,
    String refreshToken
) {
    public static IdPwLoginResponse from(IdPwLoginResult result) {
        return IdPwLoginResponse.builder()
            .memberId(result.memberId())
            .accessToken(result.accessToken())
            .refreshToken(result.refreshToken())
            .build();
    }
}
