package com.umc.product.authentication.application.port.out;

public interface RevokeOAuthTokenPort {
    void revokeAppleToken(String refreshToken);
}
