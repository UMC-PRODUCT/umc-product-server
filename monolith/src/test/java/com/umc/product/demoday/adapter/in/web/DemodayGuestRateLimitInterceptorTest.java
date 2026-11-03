package com.umc.product.demoday.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.global.config.InterceptorPipelineMockMvc;
import com.umc.product.global.config.InterceptorPipelineMockMvc.RequestLogCapture;
import com.umc.product.global.exception.constant.CommonErrorCode;
import com.umc.product.global.ratelimit.ApiRateLimitMetrics;
import com.umc.product.global.ratelimit.ApiRateLimitProperties;
import com.umc.product.global.ratelimit.RateLimitBucketRegistry;
import com.umc.product.global.ratelimit.RateLimitPolicy;
import com.umc.product.global.response.ApiErrorResponseWriter;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class DemodayGuestRateLimitInterceptorTest {

    @Test
    @DisplayName("같은 IP가 분당 한도를 넘기면 429를 반환하고, 다른 IP는 영향받지 않는다")
    void blockSameIpAfterLimitExceeded() throws Exception {
        // 초당 한도는 넉넉하게 둬서 분당 한도만 단독으로 소진되게 만든다
        // 실제 시각에 의존하지 않고 같은 초 안의 연속 호출만으로 결정적으로 재현하기 위함.
        MockMvc mockMvc = mockMvcWithPolicy(new RateLimitPolicy("demoday-guest-participation", 10, 2));

        mockMvc.perform(post("/guest").with(request -> {
                request.setRemoteAddr("10.0.0.1");
                return request;
            }))
            .andExpect(status().isOk())
            .andExpect(header().string("X-RateLimit-Limit", "2"));

        mockMvc.perform(post("/guest").with(request -> {
                request.setRemoteAddr("10.0.0.1");
                return request;
            }))
            .andExpect(status().isOk());

        mockMvc.perform(post("/guest").with(request -> {
                request.setRemoteAddr("10.0.0.1");
                return request;
            }))
            .andExpect(status().isTooManyRequests())
            .andExpect(header().exists("Retry-After"))
            .andExpect(jsonPath("$.code").value(CommonErrorCode.TOO_MANY_REQUESTS.getCode()));

        mockMvc.perform(post("/guest").with(request -> {
                request.setRemoteAddr("10.0.0.2");
                return request;
            }))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로깅 configurer가 먼저 등록돼도 차단된 게스트 요청은 요청 로그 없이 X-Trace-Id를 담은 429를 받는다")
    void blockedGuestRequestSkipsRequestLogsAndKeepsTraceId() throws Exception {
        // given
        DemodayWebConfig demodayWebConfig = new DemodayWebConfig(
            new StaticListableBeanFactory(Map.of(
                "interceptor", interceptor(new RateLimitPolicy("demoday-guest-participation", 10, 1))
            )).getBeanProvider(DemodayGuestRateLimitInterceptor.class)
        );
        MockMvc mockMvc = InterceptorPipelineMockMvc.build(
            List.of(InterceptorPipelineMockMvc.webMvcConfig(null), demodayWebConfig),
            new GuestParticipationController()
        );

        try (RequestLogCapture requestLogs = new RequestLogCapture()) {
            // when
            mockMvc.perform(post("/api/v1/demoday/polls/1/participations/guest"))
                .andExpect(status().isOk());
            mockMvc.perform(post("/api/v1/demoday/polls/1/participations/guest"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("X-Trace-Id", InterceptorPipelineMockMvc.TRACE_ID));

            // then
            assertThat(requestLogs.events()).containsExactly("api_request_started", "api_request_completed");
        }
    }

    private static MockMvc mockMvcWithPolicy(RateLimitPolicy policy) {
        return MockMvcBuilders.standaloneSetup(new GuestController())
            .addInterceptors(interceptor(policy))
            .build();
    }

    private static DemodayGuestRateLimitInterceptor interceptor(RateLimitPolicy policy) {
        ApiRateLimitProperties properties = ApiRateLimitProperties.defaults();
        return new DemodayGuestRateLimitInterceptor(
            new RateLimitBucketRegistry(properties),
            new ApiErrorResponseWriter(new ObjectMapper()),
            new ApiRateLimitMetrics(new SimpleMeterRegistry()),
            policy
        );
    }

    @Controller
    @ResponseBody
    private static class GuestController {

        @PostMapping("/guest")
        void submit() {
        }
    }

    @Controller
    @ResponseBody
    private static class GuestParticipationController {

        @PostMapping("/api/v1/demoday/polls/{pollId}/participations/guest")
        void submit(@PathVariable Long pollId) {
        }
    }
}
