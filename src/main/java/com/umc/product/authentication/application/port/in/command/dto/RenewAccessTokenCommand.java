package com.umc.product.authentication.application.port.in.command.dto;

public record RenewAccessTokenCommand(
        String refreshToken
) {
}
