package com.umc.product.authentication.adapter.in.web.dto.request;

public record AppleLoginRequest(
    String idToken,
    String accessToken,
    String refreshToken
) {
}
