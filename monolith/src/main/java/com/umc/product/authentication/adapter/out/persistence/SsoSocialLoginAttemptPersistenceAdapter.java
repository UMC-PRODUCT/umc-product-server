package com.umc.product.authentication.adapter.out.persistence;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.umc.product.authentication.application.port.out.SsoSocialLoginAttemptPort;
import com.umc.product.authentication.domain.SsoSocialLoginAttempt;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SsoSocialLoginAttemptPersistenceAdapter implements SsoSocialLoginAttemptPort {
    private final SsoSocialLoginAttemptJpaRepository repository;

    @Override
    public void save(SsoSocialLoginAttempt attempt) {
        repository.save(attempt);
    }

    @Override
    public Optional<SsoSocialLoginAttempt> findByStateHashForUpdate(String stateHash) {
        return repository.findByStateHash(stateHash);
    }

    @Override
    public int deleteExpired(Instant now) {
        return repository.deleteExpired(now);
    }
}
