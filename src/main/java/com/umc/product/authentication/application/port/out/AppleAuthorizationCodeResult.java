package com.umc.product.authentication.application.port.out;

import com.umc.product.authentication.adapter.in.oauth.OAuth2Attributes;

/**
 * Apple Authorization Code 교환 결과.
 *
 * @param attrs        OAuth2 attributes
 * @param refreshToken Apple refresh token (revoke 시 필요)
 * @param clientId     교환 시 사용된 Apple client_id (revoke 시 동일한 값을 사용해야 하므로 DB에 함께 보관)
 */
public record AppleAuthorizationCodeResult(OAuth2Attributes attrs, String refreshToken, String clientId) {
}