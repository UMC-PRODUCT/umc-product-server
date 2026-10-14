package com.umc.product.authentication.application.port.in.command;

import com.umc.product.authentication.application.port.in.command.dto.CompleteSsoSocialLoginCommand;
import com.umc.product.authentication.application.port.in.command.dto.SsoSocialLoginStartInfo;
import com.umc.product.authentication.application.port.in.command.dto.StartSsoSocialLoginCommand;

public interface ManageSsoSocialLoginUseCase {
    SsoSocialLoginStartInfo start(StartSsoSocialLoginCommand command);

    String complete(CompleteSsoSocialLoginCommand command);
}
