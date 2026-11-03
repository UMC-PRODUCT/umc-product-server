package com.umc.product.global.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.umc.product.support.web.InterceptorPipelineMockMvc;
import com.umc.product.support.web.RequestLogCapture;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class TraceIdHeaderInterceptorTest {

    private final TraceIdHeaderInterceptor interceptor = new TraceIdHeaderInterceptor();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("MDC 에 traceId 가 있으면 X-Trace-Id 헤더로 노출하고 별도 X-Request-Id 는 만들지 않는다")
    void exposesTraceIdAsResponseHeader() {
        // given
        MDC.put("traceId", "test-trace-abc123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        boolean result = interceptor.preHandle(new MockHttpServletRequest("GET", "/forms"), response, new Object());

        // then
        assertThat(result).isTrue();
        assertThat(response.getHeader("X-Trace-Id")).isEqualTo("test-trace-abc123");
        assertThat(response.getHeader("X-Request-Id")).isNull();
    }

    @Test
    @DisplayName("traceId 가 없으면 X-Trace-Id 헤더도 비어 있어야 한다")
    void skipsHeaderWithoutTraceId() {
        // given
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        interceptor.preHandle(new MockHttpServletRequest("GET", "/forms"), response, new Object());

        // then
        assertThat(response.getHeader("X-Trace-Id")).isNull();
    }

    @Test
    @DisplayName("order 없이 먼저 등록한 차단 인터셉터도 trace id 헤더 뒤, 로깅 앞에서 실행된다")
    void unorderedBlockingInterceptorRunsBetweenTraceIdAndLogging() throws Exception {
        // given
        WebMvcConfigurer unorderedBlocker = new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new HandlerInterceptor() {
                    @Override
                    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                        return false;
                    }
                });
            }
        };
        MockMvc mockMvc = InterceptorPipelineMockMvc.build(
            List.of(unorderedBlocker, InterceptorPipelineMockMvc.webMvcConfig()),
            new PingController()
        );

        try (RequestLogCapture requestLogs = new RequestLogCapture()) {
            // when
            mockMvc.perform(get("/ping"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("X-Trace-Id", InterceptorPipelineMockMvc.TRACE_ID));

            // then
            assertThat(requestLogs.events()).isEmpty();
        }
    }

    @RestController
    private static class PingController {

        @GetMapping("/ping")
        String ping() {
            return "pong";
        }
    }
}
