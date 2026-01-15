package com.umc.product.authentication.application.port.in.command;

import com.umc.product.authentication.application.port.in.command.dto.RenewAccessTokenCommand;

public interface ManageAuthenticationUseCase {
    /**
     * Refresh Token을 이용해서 Access Token을 재발급 합니다.
     */
    String renewAccessToken(RenewAccessTokenCommand command);
}
