package com.umc.product.global.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.global.client.ClientContextProperties;
import com.umc.product.global.client.ClientOriginRegistry;
import com.umc.product.global.client.ClientRequestClassifier;
import com.umc.product.global.config.LoggingInterceptor;
import com.umc.product.global.exception.constant.CommonErrorCode;
import com.umc.product.global.logging.OperationalMetrics;
import com.umc.product.global.response.ApiErrorResponseWriter;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class ApiRateLimitInterceptorTest {

    @Test
    @DisplayName("path variable 값이 달라도 같은 route pattern bucket을 공유해 한도 초과 시 429를 반환한다")
    void blocks_by_route_pattern_bucket() throws Exception {
        ProductController controller = new ProductController();
        MockMvc mockMvc = mockMvc(controller, limitedProperties(true));

        mockMvc.perform(get("/api/v1/products/1"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/products/2"))
            .andExpect(status().isTooManyRequests())
            .andExpect(header().exists("Retry-After"))
            .andExpect(header().string("X-RateLimit-Limit", "1"))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value(CommonErrorCode.TOO_MANY_REQUESTS.getCode()));

        assertThat(controller.invocations()).isEqualTo(1);
    }

    @Test
    @DisplayName("rate limiter가 비활성화되면 같은 bucket 요청도 모두 통과한다")
    void disabled_limiter_allows_all_requests() throws Exception {
        ProductController controller = new ProductController();
        MockMvc mockMvc = mockMvc(controller, limitedProperties(false));

        mockMvc.perform(get("/api/v1/products/1"))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/products/2"))
            .andExpect(status().isOk());

        assertThat(controller.invocations()).isEqualTo(2);
    }

    @Test
    @DisplayName("OPTIONS preflight 요청은 토큰을 소비하지 않는다")
    void options_request_is_excluded() throws Exception {
        ProductController controller = new ProductController();
        MockMvc mockMvc = mockMvc(controller, limitedProperties(true));

        mockMvc.perform(options("/api/v1/products/1"))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/products/1"))
            .andExpect(status().isOk());

        assertThat(controller.invocations()).isEqualTo(1);
    }

    private static MockMvc mockMvc(ProductController controller, ApiRateLimitProperties properties) {
        ApiRateLimitMetrics metrics = new ApiRateLimitMetrics(new SimpleMeterRegistry());
        ApiRateLimitInterceptor rateLimitInterceptor = new ApiRateLimitInterceptor(
            new RateLimitClientKeyResolver(),
            new RateLimitRouteResolver(),
            new RateLimitPolicyResolver(properties),
            new RateLimitBucketRegistry(properties),
            new ApiErrorResponseWriter(new ObjectMapper()),
            metrics
        );

        return MockMvcBuilders
            .standaloneSetup(controller)
            .addInterceptors(loggingInterceptor(), rateLimitInterceptor)
            .setMessageConverters(new MappingJackson2HttpMessageConverter())
            .build();
    }

    private static LoggingInterceptor loggingInterceptor() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        ClientContextProperties properties = new ClientContextProperties(List.of());
        return new LoggingInterceptor(
            new ClientRequestClassifier(new ClientOriginRegistry(properties)),
            new OperationalMetrics(registry)
        );
    }

    private static ApiRateLimitProperties limitedProperties(boolean enabled) {
        ApiRateLimitProperties.Limit oneRequest = new ApiRateLimitProperties.Limit(1, 1);
        return new ApiRateLimitProperties(
            enabled,
            List.of("/api/**"),
            ApiRateLimitProperties.defaultExcludedPaths(),
            oneRequest,
            oneRequest,
            List.of(),
            new ApiRateLimitProperties.Cache(10_000, Duration.ofMinutes(10))
        );
    }

    @RestController
    private static class ProductController {

        private final AtomicInteger invocations = new AtomicInteger();

        @GetMapping("/api/v1/products/{productId}")
        String product(@PathVariable Long productId) {
            invocations.incrementAndGet();
            return "product-" + productId;
        }

        @RequestMapping(method = RequestMethod.OPTIONS, path = "/api/v1/products/{productId}")
        void options() {
        }

        int invocations() {
            return invocations.get();
        }
    }
}
