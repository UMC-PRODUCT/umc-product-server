package com.umc.product.authentication.adapter.in.web.dto.request;

import com.umc.product.authentication.application.port.in.command.dto.RenewAccessTokenCommand;

public record RenewAccessTokenRequest(
    String refreshToken
) {
    public RenewAccessTokenCommand toCommand() {
        return RenewAccessTokenCommand.builder()
            .refreshToken(this.refreshToken)
            .build();
    }
}
