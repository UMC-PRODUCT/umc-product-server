package com.umc.product.member.adapter.in.web.dto.response;

import lombok.Builder;

@Builder
public record RegisterResponse(
    Long memberId,
    String accessToken,
    String refreshToken
) {
    public static RegisterResponse of(Long memberId, String accessToken, String refreshToken) {
        return RegisterResponse.builder()
            .memberId(memberId)
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }
}
