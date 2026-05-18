package com.umc.product.authentication.application.port.out;

public interface RevokeOAuthTokenPort {
    /**
     * Apple refresh token을 revoke합니다.
     *
     * @param refreshToken Apple refresh token
     * @param clientId     해당 refresh token을 발급받을 때 사용한 client_id
     */
    void revokeAppleToken(String refreshToken, String clientId);

    /**
     * Kakao Access Token을 사용하여 앱과 사용자의 연결을 해제합니다.
     *
     * @param accessToken Kakao Access Token
     */
    void revokeKakaoToken(String accessToken);

    /**
     * Google Access Token을 revoke합니다.
     *
     * @param accessToken Google Access Token
     */
    void revokeGoogleToken(String accessToken);
}