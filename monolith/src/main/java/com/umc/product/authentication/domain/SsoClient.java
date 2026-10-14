package com.umc.product.authentication.domain;

import java.time.Duration;
import java.util.List;

import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.common.domain.enums.ClientType;
import com.umc.product.global.client.ClientEnvironment;
import com.umc.product.global.client.ClientServiceType;

public record SsoClient(
    String clientId,
    String name,
    ClientServiceType serviceType,
    ClientEnvironment environment,
    boolean requirePkce,
    Duration accessTokenTtl,
    List<String> redirectUris,
    List<String> allowedOrigins
) {
    public SsoClient {
        if (clientId == null || clientId.isBlank()) {
            throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_SSO_CLIENT);
        }
        serviceType = serviceType == null ? ClientServiceType.UNKNOWN : serviceType;
        environment = environment == null ? ClientEnvironment.UNKNOWN : environment;
        redirectUris = copyRedirectUris(redirectUris);
        allowedOrigins = copyAllowedOrigins(allowedOrigins);
    }

    public static SsoClient of(
        String clientId,
        String name,
        ClientServiceType serviceType,
        ClientEnvironment environment,
        boolean requirePkce,
        Duration accessTokenTtl,
        List<String> redirectUris,
        List<String> allowedOrigins
    ) {
        return new SsoClient(
            clientId,
            name,
            serviceType,
            environment,
            requirePkce,
            accessTokenTtl,
            redirectUris,
            allowedOrigins
        );
    }

    /**
     * 등록 서비스 종류를 토큰의 플랫폼으로 변환한다. MACOS_APP은 서비스 구분, MACOS는 실행 플랫폼 구분이다.
     * 최초 SSO 교환과 refresh 모두 이 규칙을 사용해야 갱신 후 플랫폼 정보가 누락되거나 WEB으로 바뀌지 않는다.
     */
    public ClientType tokenClientType() {
        return switch (serviceType) {
            case IOS_APP -> ClientType.IOS;
            case ANDROID_APP -> ClientType.ANDROID;
            case MACOS_APP -> ClientType.MACOS;
            default -> ClientType.WEB;
        };
    }

    /** 웹 origin 대신 등록 앱 callback과 PKCE를 사용하는 네이티브 클라이언트를 판별한다. */
    public boolean isNativeApp() {
        return serviceType == ClientServiceType.IOS_APP
            || serviceType == ClientServiceType.ANDROID_APP
            || serviceType == ClientServiceType.MACOS_APP;
    }

    public boolean allowsRedirectUri(String redirectUri) {
        return redirectUri != null && redirectUris.contains(redirectUri);
    }

    public boolean allowsOrigin(String origin) {
        String normalizedOrigin = normalizeOrigin(origin);
        return normalizedOrigin != null && allowedOrigins.stream()
            .map(SsoClient::normalizeOrigin)
            .anyMatch(normalizedOrigin::equals);
    }

    private static List<String> copyRedirectUris(List<String> redirectUris) {
        if (redirectUris == null || redirectUris.isEmpty() || containsBlank(redirectUris)) {
            throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_SSO_REDIRECT_URI);
        }
        return List.copyOf(redirectUris);
    }

    private static List<String> copyAllowedOrigins(List<String> allowedOrigins) {
        if (allowedOrigins == null) {
            return List.of();
        }
        if (containsBlank(allowedOrigins)) {
            throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_SSO_CLIENT);
        }
        return List.copyOf(allowedOrigins);
    }

    private static boolean containsBlank(List<String> values) {
        return values.stream()
            .anyMatch(value -> value == null || value.isBlank());
    }

    private static String normalizeOrigin(String origin) {
        if (origin == null || origin.isBlank()) {
            return null;
        }
        String normalized = origin.trim();
        while (normalized.endsWith("/") && normalized.length() > 1) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
