package com.umc.product.authentication.adapter.in.web.dto.request;

import com.umc.product.authentication.application.port.in.command.dto.LoginByEmailCommand;
import com.umc.product.common.domain.enums.ClientType;
import jakarta.validation.constraints.NotBlank;

public record LoginByEmailRequest(
    @NotBlank String email,
    @NotBlank String password,

    // 클라이언트 플랫폼(ANDROID/IOS/WEB). 트래픽 분포 분석을 위해 AT claim 으로 흘려보낸다.
    // 도입 이전 클라이언트 호환을 위해 nullable. 누락 시 다운스트림(MDC / 통계)에서는 "UNKNOWN" 으로 집계된다.
    ClientType clientType
) {
    public LoginByEmailCommand toCommand() {
        return LoginByEmailCommand.of(this.email, this.password, this.clientType);
    }

    @Override
    public String toString() {
        return "LoginByEmailRequest[email=" + email + ", password=***, clientType=" + clientType + "]";
    }
}
