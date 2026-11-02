package com.umc.product.global.config;

import java.util.List;
import java.util.stream.Stream;

public final class SecurityPathConfig {

    public static final String SCALAR_ENTRY_PATH = "/docs";
    public static final String SCALAR_ENTRY_SLASH_PATH = "/docs/";
    public static final String ASYNCAPI_ENTRY_PATH = "/docs/asyncapi";
    public static final String ASYNCAPI_ENTRY_SLASH_PATH = "/docs/asyncapi/";
    public static final String ASYNCAPI_HTML_PATH = "/docs/asyncapi.html";
    public static final String ASYNCAPI_DOCUMENT_PATH = "/docs/asyncapi.yaml";
    public static final String SCALAR_DOCUMENTATION_PATTERN = "/docs/**";
    public static final String OPENAPI_JSON_PATH = "/docs-json";
    public static final String OPENAPI_JSON_PATTERN = "/docs-json/**";
    public static final String MARKDOWN_IT_WEBJAR_PATTERN = "/webjars/markdown-it/**";
    public static final String UMC_LOGO_PATH = "/umc-logo.svg";
    public static final String GRAPHQL_PATH = "/graphql";
    public static final String GRAPHIQL_PATH = "/graphiql";
    public static final String GRAPHIQL_PATTERN = "/graphiql/**";
    public static final String WELL_KNOWN_PATTERN = "/.well-known/**";
    public static final String COMMUNITY_THREAD_DEEP_LINK_PATTERN = "/community/threads/**";

    public static final List<String> SWAGGER_BLOCKED_PATHS = List.of(
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs",
        "/v3/api-docs/**",
        "/swagger-resources/**",
        "/webjars/swagger-ui/**"
    );

    public static final List<String> DOCUMENTATION_PATHS = List.of(
        SCALAR_ENTRY_PATH,
        SCALAR_ENTRY_SLASH_PATH,
        SCALAR_DOCUMENTATION_PATTERN,
        OPENAPI_JSON_PATH,
        OPENAPI_JSON_PATTERN,
        MARKDOWN_IT_WEBJAR_PATTERN,
        UMC_LOGO_PATH
    );

    public static final List<String> GRAPHQL_PUBLIC_PATHS = List.of(
        GRAPHQL_PATH,
        GRAPHIQL_PATH,
        GRAPHIQL_PATTERN
    );

    public static final List<String> SECURITY_PERMIT_ALL_PATHS = Stream.concat(
        Stream.concat(
            Stream.of(
                "/actuator/**",
                "/error",
                "/ws/**",
                WELL_KNOWN_PATTERN
            ),
            DOCUMENTATION_PATHS.stream()
        ),
        GRAPHQL_PUBLIC_PATHS.stream()
    ).toList();

    public static final List<String> MAINTENANCE_ALWAYS_ALLOW_PATHS = Stream.concat(
        Stream.of(
            "/api/v1/system/status",
            "/api/v1/maintenance/admin/**",
            "/api/v1/auth/sso/oauth/**",
            "/api/v1/auth/sso/**",
            "/api/v1/auth/**",
            "/api/v1/terms",
            "/api/v1/terms/**",
            "/actuator/**",
            "/error",
            // App Link/Universal Link 소유권 검증과 공유 링크 fallback 은 점검 중에도 응답해야
            // OS 재검증 주기나 외부에 공유된 링크가 깨지지 않는다(#1268).
            WELL_KNOWN_PATTERN,
            COMMUNITY_THREAD_DEEP_LINK_PATTERN
        ),
        DOCUMENTATION_PATHS.stream()
    ).toList();

    private SecurityPathConfig() {
    }

    public static String[] securityPermitAllPaths() {
        return SECURITY_PERMIT_ALL_PATHS.toArray(String[]::new);
    }

    public static String[] swaggerBlockedPaths() {
        return SWAGGER_BLOCKED_PATHS.toArray(String[]::new);
    }

    public static String[] loggingExcludedPaths() {
        return Stream.concat(
            Stream.concat(
                Stream.of("/actuator/**"),
                DOCUMENTATION_PATHS.stream()
            ),
            GRAPHQL_PUBLIC_PATHS.stream()
        ).toArray(String[]::new);
    }
}
