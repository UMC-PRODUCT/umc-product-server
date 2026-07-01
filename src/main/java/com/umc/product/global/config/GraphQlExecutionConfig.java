package com.umc.product.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.server.TimeoutWebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlInterceptor;

import graphql.analysis.MaxQueryComplexityInstrumentation;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.execution.instrumentation.Instrumentation;

@Configuration
public class GraphQlExecutionConfig {

    @Bean
    public WebGraphQlInterceptor graphQlTimeoutWebGraphQlInterceptor(GraphQlExecutionProperties properties) {
        return new TimeoutWebGraphQlInterceptor(properties.timeout());
    }

    @Bean
    public Instrumentation graphQlMaxQueryDepthInstrumentation(GraphQlExecutionProperties properties) {
        return new MaxQueryDepthInstrumentation(properties.maxDepth());
    }

    @Bean
    public Instrumentation graphQlMaxQueryComplexityInstrumentation(GraphQlExecutionProperties properties) {
        return new MaxQueryComplexityInstrumentation(properties.maxComplexity());
    }
}
