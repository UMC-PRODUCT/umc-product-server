package com.umc.product.authentication.adapter.out.persistence;

import com.umc.product.authentication.domain.EmailVerification;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmailVerificationJpaRepository extends JpaRepository<EmailVerification, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM EmailVerification ev WHERE ev.expiresAt < :threshold")
    int deleteByExpiresAtBefore(@Param("threshold") Instant threshold);
}
