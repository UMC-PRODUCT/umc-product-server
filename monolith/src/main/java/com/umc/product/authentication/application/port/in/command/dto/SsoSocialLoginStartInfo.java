package com.umc.product.authentication.application.port.in.command.dto;

public record SsoSocialLoginStartInfo(String authorizationUri, String browserBinding) {
    @Override
    public String toString() {
        return "SsoSocialLoginStartInfo[redacted]";
    }
}
