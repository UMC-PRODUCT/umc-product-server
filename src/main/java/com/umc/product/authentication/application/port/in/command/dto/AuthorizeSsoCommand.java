package com.umc.product.authentication.application.port.in.command.dto;

import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;

public record AuthorizeSsoCommand(
    String clientId,
    String redirectUri,
    String responseType,
    String state,
    String codeChallenge,
    String codeChallengeMethod,
    String rawLoginToken,
    String requestOrigin
) {
    public AuthorizeSsoCommand {
        if (isBlank(clientId)
            || isBlank(redirectUri)
            || isBlank(responseType)
            || isBlank(state)
            || isBlank(rawLoginToken)) {
            throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_SSO_AUTHORIZATION_REQUEST);
        }
    }

    public static AuthorizeSsoCommand of(
        String clientId,
        String redirectUri,
        String responseType,
        String state,
        String codeChallenge,
        String codeChallengeMethod,
        String rawLoginToken
    ) {
        return of(
            clientId,
            redirectUri,
            responseType,
            state,
            codeChallenge,
            codeChallengeMethod,
            rawLoginToken,
            null
        );
    }

    public static AuthorizeSsoCommand of(
        String clientId,
        String redirectUri,
        String responseType,
        String state,
        String codeChallenge,
        String codeChallengeMethod,
        String rawLoginToken,
        String requestOrigin
    ) {
        return new AuthorizeSsoCommand(
            clientId,
            redirectUri,
            responseType,
            state,
            codeChallenge,
            codeChallengeMethod,
            rawLoginToken,
            requestOrigin
        );
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
