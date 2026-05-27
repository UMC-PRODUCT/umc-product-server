package com.umc.product.authentication.adapter.in.web.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.authentication.application.port.in.command.dto.LoginByEmailCommand;
import com.umc.product.common.domain.enums.ClientType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoginByEmailRequestTest {

    private static final String EMAIL = "alice@example.com";
    private static final String RAW_PASSWORD = "Strong-Pw-2026";

    @Test
    @DisplayName("이메일/PW 로그인 요청의 clientType 을 커맨드로 전달한다")
    void clientType_커맨드_전달() {
        // given
        LoginByEmailRequest request = new LoginByEmailRequest(
            EMAIL,
            RAW_PASSWORD,
            ClientType.ANDROID
        );

        // when
        LoginByEmailCommand command = request.toCommand();

        // then
        assertThat(command.email()).isEqualTo(EMAIL);
        assertThat(command.rawPassword()).isEqualTo(RAW_PASSWORD);
        assertThat(command.clientType()).isEqualTo(ClientType.ANDROID);
    }
}
