package com.umc.product.global.ratelimit;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.api-rate-limit")
public record ApiRateLimitProperties(
    Boolean enabled,
    List<String> includePaths,
    List<String> excludePaths,
    Limit authenticatedDefault,
    Limit anonymousDefault,
    List<RoutePolicy> routePolicies,
    Cache cache
) {

    public ApiRateLimitProperties {
        enabled = enabled == null || enabled;
        includePaths = copyOrDefault(includePaths, List.of("/api/**"));
        excludePaths = copyOrDefault(excludePaths, defaultExcludedPaths());
        authenticatedDefault = authenticatedDefault == null ? new Limit(20, 300) : authenticatedDefault;
        anonymousDefault = anonymousDefault == null ? new Limit(5, 60) : anonymousDefault;
        routePolicies = copyOrDefault(routePolicies, defaultRoutePolicies());
        cache = cache == null ? new Cache(100_000, Duration.ofMinutes(10)) : cache;
    }

    public static ApiRateLimitProperties defaults() {
        return new ApiRateLimitProperties(
            true,
            List.of("/api/**"),
            defaultExcludedPaths(),
            new Limit(20, 300),
            new Limit(5, 60),
            defaultRoutePolicies(),
            new Cache(100_000, Duration.ofMinutes(10))
        );
    }

    public static List<String> defaultExcludedPaths() {
        return List.of(
            "/actuator/**",
            "/docs",
            "/docs/",
            "/docs/**",
            "/docs-json",
            "/docs-json/**",
            "/webjars/**",
            "/umc-logo.svg",
            "/error"
        );
    }

    public static List<RoutePolicy> defaultRoutePolicies() {
        return List.of(
            new RoutePolicy(
                "expensive",
                List.of(
                    "/api/v1/search/**",
                    "/api/search/**",
                    "/api/v1/recommend/**",
                    "/api/recommend/**",
                    "/api/v1/report/**",
                    "/api/report/**",
                    "/api/v1/llm/**",
                    "/api/llm/**"
                ),
                List.of("GET", "POST"),
                new Limit(3, 30),
                new Limit(1, 10)
            )
        );
    }

    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }

    private static <T> List<T> copyOrDefault(List<T> values, List<T> defaults) {
        if (values == null || values.isEmpty()) {
            return List.copyOf(defaults);
        }
        return List.copyOf(values);
    }

    public record Limit(
        int requestsPerSecond,
        int requestsPerMinute
    ) {

        public Limit {
            requestsPerSecond = Math.max(1, requestsPerSecond);
            requestsPerMinute = Math.max(requestsPerSecond, requestsPerMinute);
        }
    }

    public record RoutePolicy(
        String name,
        List<String> pathPatterns,
        List<String> methods,
        Limit authenticated,
        Limit anonymous
    ) {

        public RoutePolicy {
            name = name == null || name.isBlank() ? "custom" : name.trim();
            pathPatterns = pathPatterns == null ? List.of() : List.copyOf(pathPatterns);
            methods = methods == null ? List.of() : methods.stream()
                .filter(method -> method != null && !method.isBlank())
                .map(method -> method.trim().toUpperCase())
                .toList();
        }
    }

    public record Cache(
        long maximumSize,
        Duration expireAfterAccess
    ) {

        public Cache {
            maximumSize = Math.max(1L, maximumSize);
            if (expireAfterAccess == null || expireAfterAccess.isZero() || expireAfterAccess.isNegative()) {
                expireAfterAccess = Duration.ofMinutes(10);
            }
        }
    }
}
