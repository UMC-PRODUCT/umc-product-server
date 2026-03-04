package com.umc.product.authentication.application.port.out;

public interface RevokeOAuthTokenPort {
    void revokeAppleToken(String refreshToken);

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
