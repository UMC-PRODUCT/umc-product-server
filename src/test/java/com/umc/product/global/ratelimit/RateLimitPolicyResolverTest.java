package com.umc.product.global.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RateLimitPolicyResolverTest {

    @Test
    @DisplayName("기본 설정은 활성화되어 있고 /api/** GET 요청에는 인증 사용자 기본 정책을 적용한다")
    void resolve_authenticated_default_policy() {
        RateLimitPolicyResolver resolver = new RateLimitPolicyResolver(ApiRateLimitProperties.defaults());

        RateLimitPolicy policy = resolver.resolve("GET", "/api/v1/projects/{projectId}", true).orElseThrow();

        assertThat(policy.name()).isEqualTo("authenticated-default");
        assertThat(policy.requestsPerSecond()).isEqualTo(20);
        assertThat(policy.requestsPerMinute()).isEqualTo(300);
    }

    @Test
    @DisplayName("비싼 API 경로는 더 엄격한 expensive 정책을 우선 적용한다")
    void resolve_expensive_route_policy() {
        RateLimitPolicyResolver resolver = new RateLimitPolicyResolver(ApiRateLimitProperties.defaults());

        RateLimitPolicy policy = resolver.resolve("GET", "/api/v1/search/{keyword}", true).orElseThrow();

        assertThat(policy.name()).isEqualTo("expensive");
        assertThat(policy.requestsPerSecond()).isEqualTo(3);
        assertThat(policy.requestsPerMinute()).isEqualTo(30);
    }

    @Test
    @DisplayName("OPTIONS와 제외 경로는 rate limit 대상에서 제외한다")
    void exclude_options_and_configured_paths() {
        RateLimitPolicyResolver resolver = new RateLimitPolicyResolver(ApiRateLimitProperties.defaults());

        assertThat(resolver.resolve("OPTIONS", "/api/v1/projects", false)).isEmpty();
        assertThat(resolver.resolve("GET", "/docs/scalar.html", false)).isEmpty();
        assertThat(resolver.resolve("GET", "/actuator/health", false)).isEmpty();
    }

    @Test
    @DisplayName("비활성화 설정이면 /api/** 요청도 정책을 반환하지 않는다")
    void disabled_properties_returns_empty_policy() {
        ApiRateLimitProperties properties = new ApiRateLimitProperties(
            false,
            List.of("/api/**"),
            ApiRateLimitProperties.defaultExcludedPaths(),
            new ApiRateLimitProperties.Limit(20, 300),
            new ApiRateLimitProperties.Limit(5, 60),
            ApiRateLimitProperties.defaultRoutePolicies(),
            new ApiRateLimitProperties.Cache(100_000, Duration.ofMinutes(10))
        );
        RateLimitPolicyResolver resolver = new RateLimitPolicyResolver(properties);

        assertThat(resolver.resolve("GET", "/api/v1/projects", true)).isEmpty();
    }
}
