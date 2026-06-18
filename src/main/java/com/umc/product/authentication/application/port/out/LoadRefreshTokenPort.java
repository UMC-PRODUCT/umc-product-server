package com.umc.product.authentication.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.umc.product.authentication.domain.RefreshToken;

public interface LoadRefreshTokenPort {

    Optional<RefreshToken> findByJti(UUID jti);
}
