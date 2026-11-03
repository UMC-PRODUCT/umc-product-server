package com.umc.product.global.config;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Micrometer Tracing 이 MDC 에 넣은 {@code traceId} 를 {@code X-Trace-Id} 응답 헤더로 노출한다 (ADR-016).
 *
 * <p>FE 는 이 헤더로 요청을 추적하므로, 로깅 여부와 관계없이 rate limit 차단 응답에도 붙어야 한다.
 */
@Component
public class TraceIdHeaderInterceptor implements HandlerInterceptor {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String MDC_TRACE_ID = "traceId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String traceId = MDC.get(MDC_TRACE_ID);
        if (traceId != null) {
            response.setHeader(TRACE_ID_HEADER, traceId);
        }
        return true;
    }
}
