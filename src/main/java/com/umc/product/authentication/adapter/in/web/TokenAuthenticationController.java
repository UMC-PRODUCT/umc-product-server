package com.umc.product.authentication.adapter.in.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.authentication.adapter.in.web.dto.request.LogoutRequest;
import com.umc.product.authentication.adapter.in.web.dto.request.RenewAccessTokenRequest;
import com.umc.product.authentication.adapter.in.web.dto.response.RenewAccessTokenResponse;
import com.umc.product.authentication.application.port.in.command.ManageAuthenticationUseCase;
import com.umc.product.global.security.annotation.Public;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication | 토큰", description = "로그인 토큰 재발급과 로그아웃을 다룹니다.")
public class TokenAuthenticationController {

    private final ManageAuthenticationUseCase manageAuthenticationUseCase;

    @Operation(operationId = "TOKEN-001", summary = "Access Token 재발급",
        description = """
            RefreshToken을 이용해서 AccessToken을 재발급합니다.
            Header에 AccessToken을 포함할 필요는 없지만, 만료된 토큰이나 잘못된 토큰을 401 뜨니까 주의하세요.
            """)
    @PostMapping("token/renew")
    @Public
    public RenewAccessTokenResponse renewAccessToken(
        @Valid @RequestBody RenewAccessTokenRequest request
    ) {
        return RenewAccessTokenResponse.from(
            manageAuthenticationUseCase.renewAccessToken(
                request.toCommand()
            ));
    }

    @Operation(operationId = "TOKEN-002", summary = "로그아웃",
        description = """
            RefreshToken을 서버 allow-list에서 제거합니다.
            AccessToken 없이 RefreshToken만으로 호출할 수 있으며, 만료된 AccessToken이 Authorization 헤더에 있어도 무관합니다.
            이미 제거된 토큰은 멱등하게 성공합니다.
            """)
    @PostMapping("logout")
    @Public
    public void logout(
        @Valid @RequestBody LogoutRequest request
    ) {
        manageAuthenticationUseCase.logout(request.toCommand());
    }
}
