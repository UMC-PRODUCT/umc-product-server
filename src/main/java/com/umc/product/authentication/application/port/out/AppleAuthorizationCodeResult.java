package com.umc.product.authentication.application.port.out;

import com.umc.product.authentication.adapter.in.oauth.OAuth2Attributes;

public record AppleAuthorizationCodeResult(OAuth2Attributes attrs, String refreshToken) {
}
