package com.umc.product.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

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
}
