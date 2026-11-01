package com.umc.product.authentication.application.port.out;

import com.umc.product.authentication.domain.RefreshToken;

public interface SaveRefreshTokenPort {

    RefreshToken save(RefreshToken refreshToken);
}
