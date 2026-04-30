package com.umc.product.global.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
 * <p>모든 HTTP 요청의 메서드, URI, 응답 상태 코드, 처리 시간, 쿼리 통계, 클라이언트 IP를 로깅합니다.
 */
@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTR = "startTime";
    private static final String CLIENT_IP_ATTR = "clientIp";
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID = "traceId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        request.setAttribute(START_TIME_ATTR, Instant.now());
        request.setAttribute(CLIENT_IP_ATTR, extractClientIp(request));

        QueryStatsHolder.init();

        // URI와 Query String 조합
        String requestUri = request.getRequestURI();

        String queryString = request.getQueryString();
        if (queryString != null) {
            // 인코딩된 문자열을 사람이 읽을 수 있게 디코딩
            queryString = URLDecoder.decode(queryString, StandardCharsets.UTF_8);
        }

        String fullPath = (queryString != null) ? requestUri + "?" + queryString : requestUri;

        log.info("[REQ] 💗 {} {}", request.getMethod(), fullPath);

        log.debug("[MDC] {}", MDC.getCopyOfContextMap());

        String traceId = MDC.get(TRACE_ID);
        if (traceId != null) {
            response.setHeader(TRACE_ID_HEADER, traceId);
        }

        return true;
    }

    @Override
    public void postHandle(
        HttpServletRequest request, HttpServletResponse response,
        Object handler, ModelAndView modelAndView
    ) {
        // Controller 실행 후, View 렌더링 전
    }

    @Override
    public void afterCompletion(
        HttpServletRequest request, HttpServletResponse response,
        Object handler, Exception ex
    ) {
        Instant startTime = (Instant) request.getAttribute(START_TIME_ATTR);
        if (startTime == null) {
            return;
        }

        long durationMs = Duration.between(startTime, Instant.now()).toMillis();
        long queryCount = QueryStatsHolder.getQueryCount();
        long queryTimeMs = QueryStatsHolder.getTotalTimeMs();
        String clientIp = (String) request.getAttribute(CLIENT_IP_ATTR);

        QueryStatsHolder.clear();

        String status = getStatusEmoji(response.getStatus());

        log.info("[RES] {} {} {} {}ms | Query Count: {}, Time: {}ms | IP: {}",
            status,
            response.getStatus(),
            request.getRequestURI(),
            durationMs,
            queryCount,
            queryTimeMs,
            clientIp
        );

        if (ex != null) {
            log.error("  └─ Exception: {}", ex.getMessage());
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // 다중 프록시 경유 시 첫 번째 IP가 실제 클라이언트 IP
            return xForwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
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
