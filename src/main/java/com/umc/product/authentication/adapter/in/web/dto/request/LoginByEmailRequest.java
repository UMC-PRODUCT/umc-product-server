package com.umc.product.authentication.adapter.in.web.dto.request;

import com.umc.product.authentication.application.port.in.command.dto.LoginByEmailCommand;
import jakarta.validation.constraints.NotBlank;

public record LoginByEmailRequest(
    @NotBlank String email,
    @NotBlank String password
) {
    public LoginByEmailCommand toCommand() {
        return LoginByEmailCommand.of(this.email, this.password);
    }

    @Override
    public String toString() {
        return "LoginByEmailRequest[email=" + email + ", password=***]";
    }
}
