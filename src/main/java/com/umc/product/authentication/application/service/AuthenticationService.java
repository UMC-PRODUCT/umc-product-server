package com.umc.product.authentication.application.service;

import com.umc.product.authentication.application.port.in.command.ManageAuthenticationUseCase;
import com.umc.product.authentication.application.port.in.command.dto.NewTokens;
import com.umc.product.authentication.application.port.in.command.dto.RenewAccessTokenCommand;
import com.umc.product.authentication.application.port.in.command.dto.ValidateEmailVerificationSessionCommand;
import com.umc.product.authentication.application.port.out.LoadEmailVerificationPort;
import com.umc.product.authentication.application.port.out.SaveEmailVerificationPort;
import com.umc.product.authentication.domain.EmailVerification;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.notification.application.port.in.SendEmailUseCase;
import com.umc.product.notification.application.port.in.dto.SendVerificationEmailCommand;
import java.security.SecureRandom;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements ManageAuthenticationUseCase {

    private final SendEmailUseCase sendEmailUseCase;
    private final LoadEmailVerificationPort loadEmailVerificationPort;
    private final SaveEmailVerificationPort saveEmailVerificationPort;
    private final JwtTokenProvider jwtTokenProvider;
    @Value("${app.base-url}")
    private String serverUrl;

    // TODO: EmailSendUseCase와 구분할 필요가 있습니다.
    @Override
    public NewTokens renewAccessToken(RenewAccessTokenCommand command) {
        /*
            TODO
            ---
            refresh token을 검증하는 로직을 추가할 필요성이 있음 + refresh token은 1회만 사용 가능하도록 변경할 것.
            단, RT의 마지막 사용으로부터 5분 이내에는 사용 가능하도록 함.

            이는 네트워크 이슈로 인해서 Server에 요청이 접수되었지만 클라이언트에게는 응답이 가지 않은 경우를 대비하기 위함임
         */
        
        Long memberId = jwtTokenProvider.parseRefreshToken(command.refreshToken());

        return NewTokens.builder()
            .accessToken(jwtTokenProvider.createAccessToken(memberId, null))
            .refreshToken(jwtTokenProvider.createRefreshToken(memberId))
            .build();
    }

    @Override
    @Transactional
    public Long createEmailVerificationSession(String email) {
        String code = generateRandomCode();
        String token = UUID.randomUUID().toString();

        EmailVerification emailVerification = EmailVerification.builder()
            .email(email)
            .code(code)
            .token(token)
            .build();

        Long sessionId = saveEmailVerificationPort.save(emailVerification).getId();

        sendVerificationEmail(email, code, token);

        return sessionId;
    }

    @Override
    @Transactional
    public void resendEmailVerification(Long sessionId) {
        EmailVerification emailVerification = loadEmailVerificationPort.getById(sessionId);

        String newCode = generateRandomCode();
        String newToken = UUID.randomUUID().toString();

        emailVerification.regenerate(newCode, newToken);

        sendVerificationEmail(emailVerification.getEmail(), newCode, newToken);
    }

    @Override
    @Transactional
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
            throw new AuthenticationDomainException(AuthenticationErrorCode.UNSUPPORTED_EMAIL_VERIFICATION_METHOD);
        }

        throw new AuthenticationDomainException(AuthenticationErrorCode.NO_EMAIL_VERIFICATION_METHOD_GIVEN);
    }

    private void sendVerificationEmail(String email, String code, String token) {
        String emailVerificationPath = "/api/v1/auth/email-verification/token";

        String verificationLink = UriComponentsBuilder
            .fromUriString(serverUrl)
            .path(emailVerificationPath)
            .queryParam("token", token)
            .toUriString();

        sendEmailUseCase.sendVerificationEmail(
            SendVerificationEmailCommand.builder()
                .to(email)
                .verificationCode(code)
                .verificationLink(verificationLink)
                .build()
        );
    }

    private String generateRandomCode() {
        SecureRandom random = new SecureRandom();

        return String.valueOf(random.nextInt(900000) + 100000);
    }
}
