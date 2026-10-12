package com.umc.product.authentication.adapter.in.web.dto.request;

import com.umc.product.authentication.application.port.in.command.dto.StartSsoSocialLoginCommand;
import com.umc.product.common.domain.enums.OAuthProvider;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** OAuth query 이름을 기존 token form 계약과 같은 snake_case로 받는다. */
@Getter
@Setter
public class SsoSocialStartRequest {
    @NotBlank @Size(max = 100) private String client_id;
    @NotBlank @Size(max = 500) private String redirect_uri;
    @NotBlank @Size(max = 20) private String response_type;
    @NotBlank @Size(max = 512) private String state;
    @NotBlank @Size(max = 128) private String code_challenge;
    @NotBlank @Size(max = 20) private String code_challenge_method;

    public StartSsoSocialLoginCommand toCommand(OAuthProvider provider, String binding) {
        return StartSsoSocialLoginCommand.of(provider, client_id, redirect_uri, response_type, state,
            code_challenge, code_challenge_method, binding);
    }
}
