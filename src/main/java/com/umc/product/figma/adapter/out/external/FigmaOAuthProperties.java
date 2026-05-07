package com.umc.product.figma.adapter.out.external;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Figma OAuth Authorization Code Flow 설정. authorize-uri / token-uri / refresh-uri 는 figma 공식 문서에 따른 기본값을 가진다.
 */
@ConfigurationProperties(prefix = "app.figma.oauth")
public record FigmaOAuthProperties(
    String clientId,
    String clientSecret,
    String redirectUri,
    String scope,
    String authorizeUri,
    String tokenUri,
    String refreshUri,
    String tokenEncryptionKey
) {
    public FigmaOAuthProperties {
        if (authorizeUri == null || authorizeUri.isBlank()) {
            authorizeUri = "https://www.figma.com/oauth";
        }
        if (tokenUri == null || tokenUri.isBlank()) {
            tokenUri = "https://api.figma.com/v1/oauth/token";
        }
        if (refreshUri == null || refreshUri.isBlank()) {
            refreshUri = "https://api.figma.com/v1/oauth/refresh";
        }
        if (scope == null || scope.isBlank()) {
            scope = "file_read,file_comments:read";
        }
    }
}
