package com.umc.product.authentication.adapter.in.web.dto.response;

import com.umc.product.common.domain.enums.OAuthProvider;

public record OAuthLoginResponse(
        OAuthProvider provider,
        boolean success,
        String code, // OAuth2ResultCode ENUM 이용해서 해야함
        String oAuthVerificationToken,
        String accessToken,
        String refreshToken
) {
}
