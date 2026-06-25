package com.umc.product.authentication.application.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.authentication.application.event.SsoBrowserLoginCreatedEvent;
import com.umc.product.authentication.application.port.in.command.ManageSsoBrowserLoginUseCase;
import com.umc.product.authentication.application.port.in.command.dto.LoginSsoBrowserByEmailCommand;
import com.umc.product.authentication.application.port.in.dto.SsoBrowserLoginInfo;
import com.umc.product.authentication.config.SsoProperties;
import com.umc.product.global.event.application.port.out.DomainEventPublisher;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SsoBrowserLoginCommandService implements ManageSsoBrowserLoginUseCase {

    private static final String EMAIL_AUTHENTICATION_METHOD = "email";

    private final SsoCredentialVerifier credentialVerifier;
    private final SsoLoginTokenProvider tokenProvider;
    private final SsoProperties properties;
    private final DomainEventPublisher eventPublisher;

    @Override
    @Transactional
    public SsoBrowserLoginInfo loginByEmail(LoginSsoBrowserByEmailCommand command) {
        Long memberId = credentialVerifier.verifyEmailPassword(command.email(), command.rawPassword());
        Instant expiresAt = Instant.now().plus(properties.browserLoginTtl());
        String loginToken = tokenProvider.createLoginToken(memberId, EMAIL_AUTHENTICATION_METHOD, expiresAt);

        eventPublisher.publish(SsoBrowserLoginCreatedEvent.of(memberId, EMAIL_AUTHENTICATION_METHOD));
        return SsoBrowserLoginInfo.of(memberId, loginToken, expiresAt);
    }
}
