package com.umc.product.authentication.application.port.in.command;

import com.umc.product.authentication.application.port.in.command.dto.NewTokens;
import com.umc.product.authentication.application.port.in.command.dto.RenewAccessTokenCommand;
import com.umc.product.authentication.application.port.in.command.dto.ValidateEmailVerificationSessionCommand;
import com.umc.product.authentication.domain.EmailVerificationPurpose;

public interface ManageAuthenticationUseCase {
    /**
     * Refresh Token을 이용해서 Access Token을 재발급 합니다.
     */
    NewTokens renewAccessToken(RenewAccessTokenCommand command);

    /**
     * 새로운 이메일 인증 세션을 생성합니다. (발송까지)
     * <p>
     * 세션 용도(purpose) 는 발급 시점에 고정되며, 이후 검증으로 발급되는
     * emailVerificationToken 의 purpose claim 으로 그대로 이어진다.
     */
    Long createEmailVerificationSession(String email, EmailVerificationPurpose purpose);

    /**
     * 기존 이메일 인증 세션의 인증 코드를 재발급하고 이메일을 재전송합니다.
     */
    void resendEmailVerification(Long sessionId);

    /**
     * 이메일 인증 요청을 검증합니다.
     */
    String validateEmailVerificationSession(ValidateEmailVerificationSessionCommand command);
}
