package com.umc.product.authentication.application.port.out;

import com.umc.product.authentication.domain.EmailVerification;

public interface LoadEmailVerificationPort {
    EmailVerification getById(Long id);

    EmailVerification getByToken(String token);
}
