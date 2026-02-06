package com.umc.product.global.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * HTTP 요청/응답 로깅 인터셉터
 *
 * <p>모든 HTTP 요청의 메서드, URI, 응답 상태 코드, 처리 시간을 로깅합니다.
 */
@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTR = "startTime";
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID = "traceId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTR, Instant.now());

        log.info("[REQ] {} {}", request.getMethod(), request.getRequestURI());

        log.debug("[MDC] {}", MDC.getCopyOfContextMap());

        String traceId = MDC.get(TRACE_ID);
        if (traceId != null) {
            response.setHeader(TRACE_ID_HEADER, traceId);
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) {
        // Controller 실행 후, View 렌더링 전
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Instant startTime = (Instant) request.getAttribute(START_TIME_ATTR);
        if (startTime == null) {
            return;
        }

        Duration duration = Duration.between(startTime, Instant.now());

        String status = getStatusEmoji(response.getStatus());

        log.info("[RES] {} {} {} {}ms",
            status,
            response.getStatus(),
            request.getRequestURI(),
            duration.toMillis());

        if (ex != null) {
            log.error("  └─ Exception: {}", ex.getMessage());
        }
    }

    private String getStatusEmoji(int status) {
        if (status >= 200 && status < 300) {
            return "✅";
        }
        if (status >= 300 && status < 400) {
            return "🔄";
        }
        if (status >= 400 && status < 500) {
            return "⚠️";
        }
        return "❌";
    }
}
