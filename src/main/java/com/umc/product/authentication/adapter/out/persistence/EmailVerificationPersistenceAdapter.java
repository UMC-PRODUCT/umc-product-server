package com.umc.product.authentication.adapter.out.persistence;

import com.umc.product.authentication.application.port.out.LoadEmailVerificationPort;
import com.umc.product.authentication.application.port.out.SaveEmailVerificationPort;
import com.umc.product.authentication.domain.EmailVerification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class EmailVerificationPersistenceAdapter implements LoadEmailVerificationPort, SaveEmailVerificationPort {

    private final EmailVerificationJpaRepository emailVerificationJpaRepository;
    private final EmailVerificationQueryRepository emailVerificationQueryRepository;

    @Override
    public EmailVerification getById(Long id) {
        return emailVerificationQueryRepository.findById(id);
    }

    @Override
    public EmailVerification getByToken(String token) {
        return emailVerificationQueryRepository.findByToken(token);
    }

    @Override
    public EmailVerification save(EmailVerification emailVerification) {
        return emailVerificationJpaRepository.save(emailVerification);
    }
}
