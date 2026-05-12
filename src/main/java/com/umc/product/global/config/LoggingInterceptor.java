package com.umc.product.global.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

/**
 * HTTP 요청/응답 로깅 인터셉터 (ADR-016 구조화 로그 기반)
 *
 * <p>요청 진입 시점에 {@code requestId} / {@code method} / {@code path} 를 MDC 에 push 하고,
 * 응답 완료 시점에 {@code uriTemplate} / {@code statusCode} / {@code durationMs} / {@code queryCount} /
 * {@code queryTimeMs} / {@code clientIp} 를 MDC 에 채워 {@code api_request_completed} 이벤트 한 줄을 남긴다.
 *
 * <p>MDC 누수를 막기 위해 {@link #afterCompletion} 의 finally 블록에서 반드시 {@link MDC#clear()} 를 호출한다.
 */
@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTR = "startTime";
    private static final String CLIENT_IP_ATTR = "clientIp";

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    // ===== MDC keys (ADR-016 §MDC 키 표준) =====
    private static final String MDC_REQUEST_ID = "requestId";
    private static final String MDC_METHOD = "method";
    private static final String MDC_PATH = "path";
    private static final String MDC_URI_TEMPLATE = "uriTemplate";
    private static final String MDC_STATUS_CODE = "statusCode";
    private static final String MDC_DURATION_MS = "durationMs";
    private static final String MDC_QUERY_COUNT = "queryCount";
    private static final String MDC_QUERY_TIME_MS = "queryTimeMs";
    private static final String MDC_CLIENT_IP = "clientIp";
    private static final String MDC_EVENT = "event";
    private static final String MDC_EXCEPTION = "exception";
    private static final String MDC_TRACE_ID = "traceId";

    private static final String EVENT_REQUEST_STARTED = "api_request_started";
    private static final String EVENT_REQUEST_COMPLETED = "api_request_completed";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        request.setAttribute(START_TIME_ATTR, Instant.now());
        request.setAttribute(CLIENT_IP_ATTR, extractClientIp(request));

        QueryStatsHolder.init();

        // requestId 발급 + MDC 등록 (단일 요청 식별자)
        String requestId = UUID.randomUUID().toString();
        MDC.put(MDC_REQUEST_ID, requestId);
        MDC.put(MDC_METHOD, request.getMethod());
        MDC.put(MDC_PATH, request.getRequestURI());

        // 응답 헤더로 외부에 노출 (디버깅 / 운영자 grep 용)
        response.setHeader(REQUEST_ID_HEADER, requestId);

        String traceId = MDC.get(MDC_TRACE_ID);
        if (traceId != null) {
            response.setHeader(TRACE_ID_HEADER, traceId);
        }

        log.info(EVENT_REQUEST_STARTED);
        return true;
    }

    @Override
    public void afterCompletion(
        HttpServletRequest request, HttpServletResponse response,
        Object handler, Exception ex
    ) {
        try {
            Instant startTime = (Instant) request.getAttribute(START_TIME_ATTR);
            if (startTime == null) {
                return;
            }

            long durationMs = Duration.between(startTime, Instant.now()).toMillis();
            long queryCount = QueryStatsHolder.getQueryCount();
            long queryTimeMs = QueryStatsHolder.getTotalTimeMs();
            QueryStatsHolder.clear();

            // uriTemplate: Spring 이 매칭한 패턴 (예: /forms/{formId}/answers).
            // path 가 가변이라 dashboard 의 P95/P99 집계가 불가능했던 문제를 해결한다.
            Object bestMatchingPattern = request.getAttribute(
                HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE
            );
            String uriTemplate = bestMatchingPattern instanceof String s ? s : "UNKNOWN";

            String clientIp = (String) request.getAttribute(CLIENT_IP_ATTR);

            MDC.put(MDC_EVENT, EVENT_REQUEST_COMPLETED);
            MDC.put(MDC_URI_TEMPLATE, uriTemplate);
            MDC.put(MDC_STATUS_CODE, String.valueOf(response.getStatus()));
            MDC.put(MDC_DURATION_MS, String.valueOf(durationMs));
            MDC.put(MDC_QUERY_COUNT, String.valueOf(queryCount));
            MDC.put(MDC_QUERY_TIME_MS, String.valueOf(queryTimeMs));
            if (clientIp != null) {
                MDC.put(MDC_CLIENT_IP, clientIp);
            }

            if (ex != null) {
                MDC.put(MDC_EXCEPTION, ex.getClass().getSimpleName());
                log.error(EVENT_REQUEST_COMPLETED, ex);
            } else {
                log.info(EVENT_REQUEST_COMPLETED);
            }
        } finally {
            MDC.clear();
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
}
