package com.umc.product.authentication.adapter.out.external;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.oauth2.apple")
public record AppleOAuthProperties(
    String clientId,
    String teamId,
    String keyId,
    String privateKey
) {
}
