package com.umc.product.authentication.adapter.in.web.dto.request;

import com.umc.product.authentication.application.port.in.command.dto.LogoutCommand;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
    @NotBlank String refreshToken
) {

    public LogoutCommand toCommand() {
        return LogoutCommand.from(refreshToken);
    }
}
