package com.umc.product.authentication.adapter.in.web.dto.request;

import com.umc.product.authentication.application.port.in.command.dto.LoginByIdPwCommand;
import jakarta.validation.constraints.NotBlank;

public record LoginByIdPwRequest(
    @NotBlank String loginId,
    @NotBlank String password
) {
    public LoginByIdPwCommand toCommand() {
        return LoginByIdPwCommand.of(this.loginId, this.password);
    }

    @Override
    public String toString() {
        return "LoginByIdPwRequest[loginId=" + loginId + ", password=***]";
    }
}
