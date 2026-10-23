package com.umc.product.authentication.application.port.in.command.dto;

public record LogoutCommand(
    String refreshToken
) {

    public static LogoutCommand from(String refreshToken) {
        return new LogoutCommand(refreshToken);
    }
}
