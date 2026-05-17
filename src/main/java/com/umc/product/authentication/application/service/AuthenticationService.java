package com.umc.product.authentication.application.service;

import com.umc.product.authentication.application.port.in.command.ManageAuthenticationUseCase;
import com.umc.product.authentication.application.port.in.command.dto.NewTokens;
import com.umc.product.authentication.application.port.in.command.dto.RenewAccessTokenCommand;
import com.umc.product.authentication.application.port.in.command.dto.ValidateEmailVerificationSessionCommand;
import com.umc.product.authentication.application.port.out.LoadEmailVerificationPort;
import com.umc.product.authentication.application.port.out.SaveEmailVerificationPort;
import com.umc.product.authentication.domain.CredentialPolicy;
import com.umc.product.authentication.domain.EmailVerification;
import com.umc.product.authentication.domain.EmailVerificationPurpose;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.member.application.port.in.query.GetMemberCredentialUseCase;
import com.umc.product.notification.application.port.in.SendEmailUseCase;
import com.umc.product.notification.application.port.in.dto.SendVerificationEmailCommand;
import java.security.SecureRandom;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService implements ManageAuthenticationUseCase {

    private final SendEmailUseCase sendEmailUseCase;
    private final LoadEmailVerificationPort loadEmailVerificationPort;
    private final SaveEmailVerificationPort saveEmailVerificationPort;
    private final JwtTokenProvider jwtTokenProvider;
    private final GetMemberCredentialUseCase getMemberCredentialUseCase;
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
    public Long createEmailVerificationSession(String email, EmailVerificationPurpose purpose) {
        // DTO 단계 검증을 통과해도, Service 진입 시 도메인 SSOT 인 CredentialPolicy 로 한번 더 검증한다.
        CredentialPolicy.validateEmail(email);

        // purpose 별 가입 여부 분기 검증.
        // - REGISTER: 이미 가입된 이메일이면 인증 진행 자체를 차단 (가입 마지막 단계의 UNIQUE 충돌로 인한
        //   "인증 다 했는데 실패" UX 방지). 회원가입 흐름에서는 이미 가입 여부 노출이 자연스러움.
        // - PASSWORD_RESET: 미가입 / 자격증명 미등록 이메일에 대해서는 user enumeration 방어를 위해
        //   응답은 동일하게 내려보내되 실제 메일 발송만 건너뛴다. 후속 reset 흐름에서도 INVALID_LOGIN_CREDENTIAL
        //   단일 메시지로 응답하므로 끝까지 가입 여부가 노출되지 않는다.
        boolean shouldSendEmail = switch (purpose) {
            case REGISTER -> {
                if (getMemberCredentialUseCase.existsByEmail(email)) {
                    throw new AuthenticationDomainException(AuthenticationErrorCode.EMAIL_ALREADY_EXISTS);
                }
                yield true;
            }
            case PASSWORD_RESET -> {
                boolean hasCredential = getMemberCredentialUseCase.findCredentialByEmail(email).isPresent();
                if (!hasCredential) {
                    log.info("PASSWORD_RESET 발송 요청이지만 가입/자격증명 미존재 — 발송 건너뜀 (enumeration 방어)");
                }
                yield hasCredential;
            }
        };

        String code = generateRandomCode();
        String token = UUID.randomUUID().toString();

        EmailVerification emailVerification = EmailVerification.builder()
            .email(email)
            .code(code)
            .token(token)
            .purpose(purpose)
            .build();

        Long sessionId = saveEmailVerificationPort.save(emailVerification).getId();

        if (shouldSendEmail) {
            sendVerificationEmail(email, code, token);
        }

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
    @Transactional(noRollbackFor = AuthenticationDomainException.class)
    public String validateEmailVerificationSession(ValidateEmailVerificationSessionCommand command) {
        // code가 주어지면 토큰이 우선 순위
        if (command.code() != null) {
            EmailVerification emailVerification = loadEmailVerificationPort.getById(
                Long.valueOf(command.sessionId())
            );

            // verifyCode 가 실패해도 attempt_count 증가/세션 무효화는 영속되어야 하므로
            // AuthenticationDomainException 에 대해서는 트랜잭션을 롤백하지 않는다.
            emailVerification.verifyCode(command.code());

            return jwtTokenProvider.createEmailVerificationToken(
                emailVerification.getEmail(),
                emailVerification.getPurpose()
            );
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
