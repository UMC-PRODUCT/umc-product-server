package com.umc.product.authentication.adapter.out.persistence;

import com.umc.product.authentication.domain.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationJpaRepository extends JpaRepository<EmailVerification, Long> {
}
