package com.umc.product.authentication.adapter.out.persistence;

import com.umc.product.authentication.application.port.out.DeleteEmailVerificationPort;
import com.umc.product.authentication.application.port.out.LoadEmailVerificationPort;
import com.umc.product.authentication.application.port.out.SaveEmailVerificationPort;
import com.umc.product.authentication.domain.EmailVerification;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class EmailVerificationPersistenceAdapter implements
    LoadEmailVerificationPort, SaveEmailVerificationPort, DeleteEmailVerificationPort {

    private final EmailVerificationJpaRepository emailVerificationJpaRepository;
    private final EmailVerificationQueryRepository emailVerificationQueryRepository;

    @Override
    public EmailVerification getById(Long id) {
        return emailVerificationQueryRepository.findById(id)
            .orElseThrow(() -> new AuthenticationDomainException(
                AuthenticationErrorCode.INVALID_EMAIL_VERIFICATION));
    }

    @Override
    public Optional<EmailVerification> findLatestSentByEmail(String email) {
        return emailVerificationQueryRepository.findLatestSentByEmail(email);
    }

    @Override
    public EmailVerification save(EmailVerification emailVerification) {
        return emailVerificationJpaRepository.save(emailVerification);
    }

    @Override
    public int deleteExpiredBefore(Instant threshold) {
        return emailVerificationJpaRepository.deleteByExpiresAtBefore(threshold);
    }
}
