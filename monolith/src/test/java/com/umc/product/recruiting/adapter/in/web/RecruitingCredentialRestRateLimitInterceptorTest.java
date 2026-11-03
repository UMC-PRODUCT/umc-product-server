package com.umc.product.recruiting.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.global.exception.constant.CommonErrorCode;
import com.umc.product.global.ratelimit.ApiRateLimitMetrics;
import com.umc.product.global.ratelimit.ApiRateLimitProperties;
import com.umc.product.global.ratelimit.RateLimitBucketRegistry;
import com.umc.product.global.response.ApiErrorResponseWriter;
import com.umc.product.support.web.InterceptorPipelineMockMvc;
import com.umc.product.support.web.RequestLogCapture;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class RecruitingCredentialRestRateLimitInterceptorTest {

    @Test
    @DisplayName("같은 IP의 credential 요청은 endpoint와 관계없이 전용 한도를 공유한다")
    void blockCredentialRequestsByClientIp() throws Exception {
        CredentialController controller = new CredentialController();
        MockMvc mockMvc = mockMvc(controller);

        mockMvc.perform(post("/api/v1/recruiting/public/applications/lookup")
                .with(request -> {
                    request.setRemoteAddr("10.0.0.1");
                    return request;
                }))
            .andExpect(status().isOk())
            .andExpect(header().string("X-RateLimit-Limit", "5"));

        mockMvc.perform(post("/api/v1/recruiting/public/applications/cancel")
                .with(request -> {
                    request.setRemoteAddr("10.0.0.1");
                    return request;
                }))
            .andExpect(status().isTooManyRequests())
            .andExpect(header().exists("Retry-After"))
            .andExpect(jsonPath("$.code").value(CommonErrorCode.TOO_MANY_REQUESTS.getCode()));

        mockMvc.perform(post("/api/v1/recruiting/public/applications/submit")
                .with(request -> {
                    request.setRemoteAddr("10.0.0.2");
                    return request;
                }))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("지원서 생성 요청은 credential 전용 한도를 소비하지 않고 수정 요청은 소비한다")
    void limitOnlyCredentialRequests() throws Exception {
        CredentialController controller = new CredentialController();
        MockMvc mockMvc = mockMvc(controller);

        mockMvc.perform(post("/api/v1/recruiting/public/applications"))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/recruiting/public/applications"))
            .andExpect(status().isOk());
        mockMvc.perform(put("/api/v1/recruiting/public/applications"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-RateLimit-Limit", "5"));

        mockMvc.perform(put("/api/v1/recruiting/public/applications"))
            .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("로깅 configurer가 먼저 등록돼도 차단된 credential 요청은 요청 로그 없이 X-Trace-Id를 담은 429를 받는다")
    void blockedCredentialRequestSkipsRequestLogsAndKeepsTraceId() throws Exception {
        // given
        RecruitingWebConfig recruitingWebConfig = new RecruitingWebConfig(
            new StaticListableBeanFactory(Map.of("interceptor", interceptor()))
                .getBeanProvider(RecruitingCredentialRestRateLimitInterceptor.class)
        );
        MockMvc mockMvc = InterceptorPipelineMockMvc.build(
            List.of(InterceptorPipelineMockMvc.webMvcConfig(), recruitingWebConfig),
            new CredentialController()
        );

        try (RequestLogCapture requestLogs = new RequestLogCapture()) {
            // when
            mockMvc.perform(post("/api/v1/recruiting/public/applications/lookup"))
                .andExpect(status().isOk());
            mockMvc.perform(post("/api/v1/recruiting/public/applications/cancel"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("X-Trace-Id", InterceptorPipelineMockMvc.TRACE_ID));

            // then
            assertThat(requestLogs.events()).containsExactly("api_request_started", "api_request_completed");
        }
    }

    private static MockMvc mockMvc(CredentialController controller) {
        return MockMvcBuilders.standaloneSetup(controller)
            .addInterceptors(interceptor())
            .build();
    }

    private static RecruitingCredentialRestRateLimitInterceptor interceptor() {
        ApiRateLimitProperties properties = ApiRateLimitProperties.defaults();
        return new RecruitingCredentialRestRateLimitInterceptor(
            new RateLimitBucketRegistry(properties),
            new ApiErrorResponseWriter(new ObjectMapper()),
            new ApiRateLimitMetrics(new SimpleMeterRegistry())
        );
    }

    @Controller
    @ResponseBody
    @RequestMapping("/api/v1/recruiting/public/applications")
    private static class CredentialController {

        private final AtomicInteger invocations = new AtomicInteger();

        @PostMapping
        void create() {
            invocations.incrementAndGet();
        }

        @PostMapping("/lookup")
        void lookup() {
            invocations.incrementAndGet();
        }

        @PostMapping("/submit")
        void submit() {
            invocations.incrementAndGet();
        }

        @PostMapping("/cancel")
        void cancel() {
            invocations.incrementAndGet();
        }

        @PutMapping
        void update() {
            invocations.incrementAndGet();
        }
    }
}
