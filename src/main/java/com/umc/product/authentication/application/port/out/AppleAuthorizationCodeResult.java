package com.umc.product.authentication.application.port.out;

import com.umc.product.authentication.domain.OAuthAttributes;

/**
 * Apple Authorization Code 교환 결과.
 *
 * @param attrs        OAuth2 attributes
 * @param refreshToken Apple refresh token (revoke 시 필요)
 * @param clientId     교환 시 사용된 Apple client_id (revoke 시 동일한 값을 사용해야 하므로 DB에 함께 보관)
 */
public record AppleAuthorizationCodeResult(OAuthAttributes attrs, String refreshToken, String clientId) {
}