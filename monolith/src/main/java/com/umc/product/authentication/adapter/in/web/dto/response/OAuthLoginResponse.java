package com.umc.product.authentication.adapter.in.web.dto.response;

import com.umc.product.authentication.domain.enums.OAuthResultCode;
import com.umc.product.common.domain.enums.OAuthProvider;
import lombok.Builder;

@Builder
public record OAuthLoginResponse(
    OAuthProvider provider,
    boolean success,
    String code, // OAuthResultCode ENUM 이용해서 해야함
    String oAuthVerificationToken,
    String accessToken,
    String refreshToken
) {
    public static OAuthLoginResponse ofLoginSuccess(
        OAuthProvider provider, String accessToken, String refreshToken
    ) {
        return OAuthLoginResponse.builder()
            .provider(provider)
            .success(OAuthResultCode.SUCCESS.isSuccess())
            .code(OAuthResultCode.SUCCESS.getCode())
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }

    public static OAuthLoginResponse ofRegisterRequired(
        OAuthProvider provider, String oAuthVerificationToken
    ) {
        return OAuthLoginResponse.builder()
            .provider(provider)
            .success(OAuthResultCode.REGISTER_REQUIRED.isSuccess())
            .code(OAuthResultCode.REGISTER_REQUIRED.getCode())
            .oAuthVerificationToken(oAuthVerificationToken)
            .build();
    }
}
