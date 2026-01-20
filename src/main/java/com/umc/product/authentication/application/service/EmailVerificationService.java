package com.umc.product.authentication.application.service;

import com.umc.product.authentication.application.port.in.command.ManageAuthenticationUseCase;
import com.umc.product.authentication.application.port.in.command.dto.RenewAccessTokenCommand;
import com.umc.product.authentication.application.port.in.command.dto.ValidateEmailVerificationSessionCommand;
import com.umc.product.authentication.application.port.out.LoadEmailVerificationPort;
import com.umc.product.authentication.application.port.out.SaveEmailVerificationPort;
import com.umc.product.authentication.domain.EmailVerification;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.notification.application.port.in.dto.SendVerificationEmailCommand;
import com.umc.product.notification.application.service.SendEmailService;
import java.security.SecureRandom;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class EmailVerificationService implements ManageAuthenticationUseCase {

    private final SendEmailService sendEmailService;
    private final LoadEmailVerificationPort loadEmailVerificationPort;
    private final SaveEmailVerificationPort saveEmailVerificationPort;
    private final JwtTokenProvider jwtTokenProvider;
    @Value("${app.base-url}")
    private String serverUrl;

    @Override
    public String renewAccessToken(RenewAccessTokenCommand command) {
        // TODO: refresh token을 검증하는 로직을 추가할 필요성이 있음
        Long memberId = jwtTokenProvider.parseRefreshToken(command.refreshToken());

        return jwtTokenProvider.createAccessToken(memberId, null);
    }

    @Override
    @Transactional
    public Long createEmailVerificationSession(String email) {
        // TODO: 이미 존재하는 verification이 있는지 검증 필요

        String code = generateRandomCode();
        String token = UUID.randomUUID().toString();

        EmailVerification emailVerification = EmailVerification.builder()
                .email(email)
                .code(code)
                .token(token)
                .build();

        String emailVerificationPath = "/api/v1/auth/email-verification/token";

        String verificationLink = UriComponentsBuilder
                .fromUriString(serverUrl)
                .path(emailVerificationPath)
                .queryParam("token", token)
                .toUriString();

        sendEmailService.sendVerificationEmail(
                SendVerificationEmailCommand.builder()
                        .to(email)
                        .verificationCode(code)
                        .verificationLink(verificationLink)
                        .build()
        );

        return saveEmailVerificationPort.save(emailVerification).getId();
    }

    @Override
    public String validateEmailVerificationSession(ValidateEmailVerificationSessionCommand command) {
        // code가 주어지면 토큰이 우선 순위
        if (command.code() != null) {
            EmailVerification emailVerification = loadEmailVerificationPort.getById(
                    Long.valueOf(command.sessionId())
            );

            emailVerification.verifyCode(command.code());

            return jwtTokenProvider.createEmailVerificationToken(emailVerification.getEmail());
        }

        if (command.token() != null) {
            EmailVerification emailVerification = loadEmailVerificationPort.getByToken(
                    command.token()
            );

            emailVerification.verifyToken();

            return jwtTokenProvider.createEmailVerificationToken(emailVerification.getEmail());
        }

        throw new AuthenticationDomainException(AuthenticationErrorCode.NO_EMAIL_VERIFICATION_METHOD_GIVEN);
    }

    private String generateRandomCode() {
        SecureRandom random = new SecureRandom();
        
        return String.valueOf(random.nextInt(900000) + 100000);
    }
}
