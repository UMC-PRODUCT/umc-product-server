package com.umc.product.global.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Ticker;
import com.umc.product.demoday.adapter.in.web.security.DemodayParticipationPrincipal;
import com.umc.product.demoday.adapter.in.web.support.CurrentDemodayParticipantArgumentResolver;
import com.umc.product.global.client.ClientContextProperties;
import com.umc.product.global.client.ClientOriginRegistry;
import com.umc.product.global.client.ClientRequestClassifier;
import com.umc.product.global.config.LoggingInterceptor;
import com.umc.product.global.config.WebMvcConfig;
import com.umc.product.global.exception.constant.CommonErrorCode;
import com.umc.product.global.logging.OperationalMetrics;
import com.umc.product.global.response.ApiErrorResponseWriter;
import com.umc.product.global.security.MemberPrincipal;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class ApiRateLimitInterceptorTest {

    private final Logger requestLogger = (Logger) LoggerFactory.getLogger(LoggingInterceptor.class);
    private final Logger rateLimitLogger = (Logger) LoggerFactory.getLogger(ApiRateLimitInterceptor.class);
    private final ListAppender<ILoggingEvent> requestLogs = new ListAppender<>();
    private final ListAppender<ILoggingEvent> rateLimitLogs = new ListAppender<>();
    private Level requestLoggerLevel;
    private Level rateLimitLoggerLevel;

    @BeforeEach
    void captureLogs() {
        requestLoggerLevel = requestLogger.getLevel();
        rateLimitLoggerLevel = rateLimitLogger.getLevel();
        requestLogger.setLevel(Level.INFO);
        rateLimitLogger.setLevel(Level.INFO);
        requestLogs.start();
        rateLimitLogs.start();
        requestLogger.addAppender(requestLogs);
        rateLimitLogger.addAppender(rateLimitLogs);
    }

    @AfterEach
    void clearSecurityContext() {
        requestLogger.detachAppender(requestLogs);
        rateLimitLogger.detachAppender(rateLimitLogs);
        requestLogger.setLevel(requestLoggerLevel);
        rateLimitLogger.setLevel(rateLimitLoggerLevel);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("인증된 POST /graphql 요청은 인증 사용자 기본 한도를 적용한다")
    void graphql_authenticated_request_uses_authenticated_default_policy() throws Exception {
        GraphQlController controller = new GraphQlController();
        MockMvc mockMvc = mockMvc(controller, ApiRateLimitProperties.defaults());
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(new MemberPrincipal(1L), null, List.of())
        );

        mockMvc.perform(post("/graphql"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-RateLimit-Limit", "20"));

        assertThat(controller.invocations()).isEqualTo(1);
    }

    @Test
    @DisplayName("익명 POST /graphql 요청은 익명 기본 한도 초과 시 429를 반환한다")
    void graphql_anonymous_request_returns_429_after_default_limit() throws Exception {
        GraphQlController controller = new GraphQlController();
        MockMvc mockMvc = mockMvc(controller, ApiRateLimitProperties.defaults());

        for (int request = 0; request < 5; request++) {
            mockMvc.perform(post("/graphql"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-RateLimit-Limit", "5"));
        }
        mockMvc.perform(post("/graphql"))
            .andExpect(status().isTooManyRequests())
            .andExpect(header().exists("Retry-After"))
            .andExpect(header().string("X-RateLimit-Limit", "5"))
            .andExpect(jsonPath("$.code").value(CommonErrorCode.TOO_MANY_REQUESTS.getCode()));

        assertThat(controller.invocations()).isEqualTo(5);
    }

    @Test
    @DisplayName("같은 IP의 게스트들은 참가자별 한도를 사용하고 한 게스트의 초과가 다른 게스트에 영향 주지 않는다")
    void demoday_guests_on_same_ip_use_independent_buckets() throws Exception {
        DemodayParticipationController controller = new DemodayParticipationController();
        MockMvc mockMvc = mockMvc(controller, guestLimitedProperties());

        authenticateGuest(101L);
        mockMvc.perform(get("/api/v1/demoday/polls/1/participations/me")
                .with(request -> {
                    request.setRemoteAddr("10.0.0.10");
                    return request;
                }))
            .andExpect(status().isOk())
            .andExpect(header().string("X-RateLimit-Limit", "1"));

        mockMvc.perform(get("/api/v1/demoday/polls/1/participations/me")
                .with(request -> {
                    request.setRemoteAddr("10.0.0.10");
                    return request;
                }))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.code").value(CommonErrorCode.TOO_MANY_REQUESTS.getCode()));

        authenticateGuest(202L);
        mockMvc.perform(get("/api/v1/demoday/polls/1/participations/me")
                .with(request -> {
                    request.setRemoteAddr("10.0.0.10");
                    return request;
                }))
            .andExpect(status().isOk())
            .andExpect(header().string("X-RateLimit-Limit", "1"));

        assertThat(controller.invocations()).isEqualTo(2);
    }

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

    @Test
    @DisplayName("한도를 넘은 요청은 요청 시작·완료 로그 없이 limiter key마다 차단 WARN 한 줄만 남긴다")
    void blocked_requests_skip_request_logs_and_warn_once_per_key() throws Exception {
        // given
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        MockMvc mockMvc = mockMvc(new ProductController(), limitedProperties(true), registry);
        mockMvc.perform(get("/api/v1/products/1"))
            .andExpect(status().isOk());

        // when
        for (int request = 0; request < 10; request++) {
            mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isTooManyRequests());
        }

        // then
        assertThat(requestLogs.list)
            .extracting(ApiRateLimitInterceptorTest::eventName)
            .containsExactly("api_request_started", "api_request_completed");
        assertThat(requestLogs.list.get(1).getFormattedMessage()).contains("status=200");
        assertThat(blockWarnings())
            .singleElement()
            .extracting(ILoggingEvent::getFormattedMessage)
            .asString()
            .contains("suppressedSinceLastLog=0");
        assertThat(rateLimitCount(registry, "blocked")).isEqualTo(10);
        assertThat(rateLimitCount(registry, "allowed")).isEqualTo(1);
    }

    @Test
    @DisplayName("서로 다른 IP의 차단은 limiter key마다 따로 WARN을 남겨 출발지를 구분한다")
    void blocked_requests_from_different_ips_warn_separately() throws Exception {
        // given
        MockMvc mockMvc = mockMvc(new ProductController(), limitedProperties(true));

        // when
        for (String remoteAddr : List.of("10.0.0.1", "10.0.0.2")) {
            for (int request = 0; request < 3; request++) {
                mockMvc.perform(get("/api/v1/products/1").with(servletRequest -> {
                    servletRequest.setRemoteAddr(remoteAddr);
                    return servletRequest;
                }));
            }
        }

        // then
        assertThat(blockWarnings())
            .extracting(warning -> keyHash(warning.getFormattedMessage()))
            .hasSize(2)
            .doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("rate limit이 아닌 4xx 응답은 요청 시작·완료 로그를 그대로 남긴다")
    void non_rate_limited_client_error_keeps_request_logs() throws Exception {
        // given
        MockMvc mockMvc = mockMvc(new ProductController(), limitedProperties(true));

        // when
        mockMvc.perform(get("/api/v1/products/invalid"))
            .andExpect(status().isBadRequest());

        // then
        assertThat(requestLogs.list)
            .extracting(ApiRateLimitInterceptorTest::eventName)
            .containsExactly("api_request_started", "api_request_completed");
        assertThat(requestLogs.list.get(1).getFormattedMessage()).contains("status=400");
        assertThat(blockWarnings()).isEmpty();
    }

    private List<ILoggingEvent> blockWarnings() {
        return rateLimitLogs.list.stream()
            .filter(event -> event.getLevel() == Level.WARN)
            .filter(event -> eventName(event).equals("api_rate_limit_blocked"))
            .toList();
    }

    private static String eventName(ILoggingEvent event) {
        return event.getMessage().split(" ", 2)[0];
    }

    private static String keyHash(String message) {
        return message.replaceAll(".*keyHash=(\\S+).*", "$1");
    }

    private static double rateLimitCount(MeterRegistry registry, String result) {
        return registry.get("api.rate_limit.requests.total").tag("result", result).counter().count();
    }

    private static MockMvc mockMvc(Object controller, ApiRateLimitProperties properties) {
        return mockMvc(controller, properties, new SimpleMeterRegistry());
    }

    private static MockMvc mockMvc(Object controller, ApiRateLimitProperties properties, MeterRegistry registry) {
        ApiRateLimitInterceptor rateLimitInterceptor = new ApiRateLimitInterceptor(
            new RateLimitClientKeyResolver(),
            new RateLimitRouteResolver(),
            new RateLimitPolicyResolver(properties),
            new RateLimitBucketRegistry(properties),
            new ApiErrorResponseWriter(new ObjectMapper()),
            new ApiRateLimitMetrics(registry),
            new RateLimitBlockLogSampler(Ticker.systemTicker())
        );
        WebMvcConfig webMvcConfig = new WebMvcConfig(
            null,
            new StaticListableBeanFactory().getBeanProvider(CurrentDemodayParticipantArgumentResolver.class),
            loggingInterceptor(),
            new StaticListableBeanFactory(Map.of("apiRateLimitInterceptor", rateLimitInterceptor))
                .getBeanProvider(ApiRateLimitInterceptor.class)
        );
        RegisteredInterceptors interceptors = new RegisteredInterceptors();
        webMvcConfig.addInterceptors(interceptors);

        return MockMvcBuilders
            .standaloneSetup(controller)
            .addInterceptors(interceptors.inRegistrationOrder())
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

    private static ApiRateLimitProperties guestLimitedProperties() {
        ApiRateLimitProperties.Limit oneRequest = new ApiRateLimitProperties.Limit(1, 1);
        ApiRateLimitProperties.Limit anonymousLimit = new ApiRateLimitProperties.Limit(5, 5);
        return new ApiRateLimitProperties(
            true,
            List.of("/api/**"),
            ApiRateLimitProperties.defaultExcludedPaths(),
            oneRequest,
            anonymousLimit,
            List.of(),
            new ApiRateLimitProperties.Cache(10_000, Duration.ofMinutes(10))
        );
    }

    private static void authenticateGuest(Long entryCodeId) {
        DemodayParticipationPrincipal principal = new DemodayParticipationPrincipal(entryCodeId);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, List.of())
        );
    }

    private static class RegisteredInterceptors extends InterceptorRegistry {

        HandlerInterceptor[] inRegistrationOrder() {
            return getInterceptors().toArray(HandlerInterceptor[]::new);
        }
    }

    @RestController
    private static class ProductController {

        private final AtomicInteger invocations = new AtomicInteger();

        @GetMapping("/api/v1/products/invalid")
        ResponseEntity<String> invalidProduct() {
            return ResponseEntity.badRequest().body("invalid");
        }

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

    @RestController
    private static class GraphQlController {

        private final AtomicInteger invocations = new AtomicInteger();

        @RequestMapping(method = RequestMethod.POST, path = "/graphql")
        String graphql() {
            invocations.incrementAndGet();
            return "graphql";
        }

        int invocations() {
            return invocations.get();
        }
    }

    @RestController
    private static class DemodayParticipationController {

        private final AtomicInteger invocations = new AtomicInteger();

        @GetMapping("/api/v1/demoday/polls/{pollId}/participations/me")
        String participation(@PathVariable Long pollId) {
            invocations.incrementAndGet();
            return "participation-" + pollId;
        }

        int invocations() {
            return invocations.get();
        }
    }
}
