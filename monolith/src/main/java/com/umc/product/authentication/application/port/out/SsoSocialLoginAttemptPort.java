package com.umc.product.authentication.application.port.out;

import java.time.Instant;
import java.util.Optional;

import com.umc.product.authentication.domain.SsoSocialLoginAttempt;

public interface SsoSocialLoginAttemptPort {
    void save(SsoSocialLoginAttempt attempt);

    Optional<SsoSocialLoginAttempt> findByStateHashForUpdate(String stateHash);

    int deleteExpired(Instant now);
}
