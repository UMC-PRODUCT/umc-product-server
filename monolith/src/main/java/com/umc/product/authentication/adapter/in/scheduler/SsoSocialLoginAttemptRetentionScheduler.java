package com.umc.product.authentication.adapter.in.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.umc.product.authentication.application.port.in.command.CleanUpSsoSocialLoginAttemptsUseCase;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SsoSocialLoginAttemptRetentionScheduler {
    private final CleanUpSsoSocialLoginAttemptsUseCase cleanup;

    @Scheduled(fixedDelayString = "PT1H")
    public void purge() {
        cleanup.cleanUp();
    }
}
