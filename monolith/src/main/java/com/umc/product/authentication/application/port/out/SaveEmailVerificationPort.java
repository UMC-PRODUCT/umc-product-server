package com.umc.product.authentication.application.port.out;

import com.umc.product.authentication.domain.EmailVerification;

public interface SaveEmailVerificationPort {
    EmailVerification save(EmailVerification emailVerification);
}
