package com.umc.product.authentication.adapter.in.web;

import com.umc.product.authentication.adapter.in.web.dto.request.RenewAccessTokenRequest;
import com.umc.product.authentication.adapter.in.web.dto.response.RenewAccessTokenResponse;
import com.umc.product.authentication.application.port.in.command.ManageAuthenticationUseCase;
import com.umc.product.global.security.annotation.Public;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication | 토큰", description = "OAuth 로그인 및 JWT 토큰 관련")
public class TokenAuthenticationController {

    private final ManageAuthenticationUseCase manageAuthenticationUseCase;

    @Operation(summary = "AccessToken 재발급",
        description = """
            RefreshToken을 이용해서 AccessToken을 재발급합니다.
            Header에 AccessToken을 포함할 필요는 없지만, 만료된 토큰이나 잘못된 토큰을 401 뜨니까 주의하세요.
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
