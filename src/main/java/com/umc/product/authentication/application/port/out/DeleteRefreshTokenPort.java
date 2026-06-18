package com.umc.product.authentication.application.port.out;

import java.util.UUID;

public interface DeleteRefreshTokenPort {

    boolean deleteByJti(UUID jti);
}
