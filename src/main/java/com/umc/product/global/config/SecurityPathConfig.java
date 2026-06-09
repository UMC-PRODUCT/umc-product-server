package com.umc.product.global.config;

import java.util.List;
import java.util.stream.Stream;

public final class SecurityPathConfig {

    public static final List<String> DOCUMENTATION_PATHS = List.of(
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/docs/**",
        "/v3/api-docs/**",
        "/docs-json/**",
        "/swagger-resources/**",
        "/webjars/**",
        "/umc-logo.svg"
    );

    public static final List<String> SECURITY_PERMIT_ALL_PATHS = Stream.concat(
        Stream.of(
            "/actuator/**",
            "/error"
        ),
        DOCUMENTATION_PATHS.stream()
    ).toList();

    public static final List<String> MAINTENANCE_ALWAYS_ALLOW_PATHS = Stream.concat(
        Stream.of(
            "/api/v1/system/status",
            "/api/v1/admin/maintenance/**",
            "/api/v1/auth/**",
            "/api/v1/terms",
            "/api/v1/terms/**",
            "/actuator/**"
        ),
        DOCUMENTATION_PATHS.stream()
    ).toList();

    private SecurityPathConfig() {
    }

    public static String[] securityPermitAllPaths() {
        return SECURITY_PERMIT_ALL_PATHS.toArray(String[]::new);
    }
}
