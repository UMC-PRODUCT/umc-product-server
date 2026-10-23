package com.umc.product.authentication.application.port.in.command.dto;

import com.umc.product.common.domain.enums.OAuthProvider;

/**
 * 제공자 callback 입력이다. code는 제공자 코드이고 state는 서버가 제공자에게 보냈던 값이다.
 * 시작 요청의 Mac state와 구별하며, browserBinding 쿠키까지 대조한 뒤에만 앱 복귀를 허용한다.
 */
public record CompleteSsoSocialLoginCommand(OAuthProvider provider, String code, String state,
    String error, String browserBinding) {
    public static CompleteSsoSocialLoginCommand of(OAuthProvider provider, String code, String state,
        String error, String binding) {
        return new CompleteSsoSocialLoginCommand(provider, code, state, error, binding);
    }

    @Override
    public String toString() {
        return "CompleteSsoSocialLoginCommand[redacted]";
    }
}
