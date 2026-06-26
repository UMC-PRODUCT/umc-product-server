package com.umc.product.authentication.adapter.in.web;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.authentication.application.port.in.command.AuthorizeSsoUseCase;
import com.umc.product.authentication.application.port.in.command.dto.AuthorizeSsoCommand;
import com.umc.product.authentication.application.port.in.command.dto.SsoAuthorizationRedirectInfo;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.global.security.annotation.Public;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/oauth")
@Tag(name = "Authentication | SSO OAuth", description = "SSO OAuth Authorization Code Flow를 처리합니다.")
public class SsoOAuthController {

    private final AuthorizeSsoUseCase authorizeSsoUseCase;

    @Public
    @GetMapping("/authorize")
    @Operation(operationId = "SSO-OAUTH-001", summary = "SSO OAuth authorization code 발급")
    public ResponseEntity<Void> authorize(
        @RequestParam("client_id") String clientId,
        @RequestParam("redirect_uri") String redirectUri,
        @RequestParam("response_type") String responseType,
        @RequestParam("state") String state,
        @RequestParam("code_challenge") String codeChallenge,
        @RequestParam("code_challenge_method") String codeChallengeMethod,
        @CookieValue(name = "${app.sso.cookie.name}", required = false) String rawLoginToken
    ) {
        if (!StringUtils.hasText(rawLoginToken)) {
            throw new AuthenticationDomainException(AuthenticationErrorCode.SSO_BROWSER_LOGIN_REQUIRED);
        }

        SsoAuthorizationRedirectInfo redirectInfo = authorizeSsoUseCase.authorize(AuthorizeSsoCommand.of(
            clientId,
            redirectUri,
            responseType,
            state,
            codeChallenge,
            codeChallengeMethod,
            rawLoginToken
        ));

        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(redirectInfo.redirectUri()))
            .build();
    }
}
