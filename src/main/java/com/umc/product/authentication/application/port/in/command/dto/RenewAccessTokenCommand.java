package com.umc.product.authentication.application.port.in.command.dto;

import lombok.Builder;

@Builder
public record RenewAccessTokenCommand(
        String refreshToken
) {
}
