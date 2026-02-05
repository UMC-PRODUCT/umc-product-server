package com.umc.product.authentication.application.port.in.command;

import com.umc.product.authentication.application.port.in.command.dto.NewTokens;
import com.umc.product.authentication.application.port.in.command.dto.RenewAccessTokenCommand;
import com.umc.product.authentication.application.port.in.command.dto.ValidateEmailVerificationSessionCommand;

public interface ManageAuthenticationUseCase {
    /**
     * Refresh Token을 이용해서 Access Token을 재발급 합니다.
     */
    NewTokens renewAccessToken(RenewAccessTokenCommand command);

    /**
     * 새로운 이메일 인증 세션을 생성합니다. (발송까지)
     */
    Long createEmailVerificationSession(String email);

    /**
     * 이메일 인증 요청을 검증합니다.
     */
    String validateEmailVerificationSession(ValidateEmailVerificationSessionCommand command);
}
