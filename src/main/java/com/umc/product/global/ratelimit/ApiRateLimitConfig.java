package com.umc.product.global.ratelimit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.umc.product.global.response.ApiErrorResponseWriter;

@Configuration
public class ApiRateLimitConfig {

    @Bean
    public ApiRateLimitInterceptor apiRateLimitInterceptor(
        RateLimitClientKeyResolver clientKeyResolver,
        RateLimitRouteResolver routeResolver,
        RateLimitPolicyResolver policyResolver,
        RateLimitBucketRegistry bucketRegistry,
        ApiErrorResponseWriter errorResponseWriter,
        ApiRateLimitMetrics metrics
    ) {
        return new ApiRateLimitInterceptor(
            clientKeyResolver,
            routeResolver,
            policyResolver,
            bucketRegistry,
            errorResponseWriter,
            metrics
        );
    }
}
