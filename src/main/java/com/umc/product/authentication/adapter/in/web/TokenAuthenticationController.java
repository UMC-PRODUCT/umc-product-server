package com.umc.product.authentication.adapter.in.web;

import com.umc.product.authentication.adapter.in.web.dto.request.RenewAccessTokenRequest;
import com.umc.product.authentication.adapter.in.web.dto.response.RenewAccessTokenResponse;
import com.umc.product.authentication.application.port.in.command.ManageAuthenticationUseCase;
import com.umc.product.global.security.annotation.Public;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class TokenAuthenticationController {

    private final ManageAuthenticationUseCase manageAuthenticationUseCase;

    @Operation(summary = "AccessToken 재발급",
        description = """
            RefreshToken을 이용해서 AccessToken을 재발급합니다.
            """)
    @PostMapping("token/renew")
    @Public
    public RenewAccessTokenResponse renewAccessToken(
        @RequestBody RenewAccessTokenRequest request
    ) {
        return RenewAccessTokenResponse.from(
            manageAuthenticationUseCase.renewAccessToken(
                request.toCommand()
            ));
    }
}
