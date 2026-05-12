package com.umc.product.global.config;

import com.umc.product.global.security.MemberPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

/**
 * HTTP 요청/응답 로깅 인터셉터 (ADR-016 구조화 로그 기반)
 *
 * <p>요청 진입 시점에 {@code method} / {@code path} 를 MDC 에 push 하고, 응답 완료 시점에
 * {@code uriTemplate} / {@code statusCode} / {@code durationMs} / {@code queryCount} /
 * {@code queryTimeMs} / {@code clientIp} 를 MDC 에 채워 {@code api_request_completed} 이벤트 한 줄을 남긴다.
 *
 * <p>단일 요청 식별자는 Micrometer Tracing 이 자동으로 MDC 에 넣는 {@code traceId} 를 그대로 재사용한다.
 * 별도의 UUID 기반 requestId 는 발급하지 않는다 — FE 등 외부 시스템이 이미 {@code X-Trace-Id} 응답 헤더로
 * 알고 있는 식별자를 변경하지 않기 위해서다.
 *
 * <p>MDC 누수를 막기 위해 {@link #afterCompletion} 의 finally 블록에서 반드시 {@link MDC#clear()} 를 호출한다.
 */
@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTR = "startTime";
    private static final String CLIENT_IP_ATTR = "clientIp";

    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    // ===== MDC keys (ADR-016 §MDC 키 표준) =====
    private static final String MDC_MEMBER_ID = "memberId";
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

        MDC.put(MDC_METHOD, request.getMethod());
        MDC.put(MDC_PATH, request.getRequestURI());

        // Micrometer Tracing 이 채워 둔 traceId 를 FE 디버깅용으로 응답 헤더에 노출.
        // 별도 requestId UUID 는 발급하지 않는다 (FE 호환성 — X-Trace-Id 가 이미 사용 중).
        String traceId = MDC.get(MDC_TRACE_ID);
        if (traceId != null) {
            response.setHeader(TRACE_ID_HEADER, traceId);
        }

        // 인증된 사용자라면 memberId 를 MDC 에 등록.
        // 익명 사용자는 비워둔다 — Loki 에서 `memberId is null` 로 익명 트래픽을 식별한다.
        putMemberIdToMdcIfAuthenticated();

        log.info(EVENT_REQUEST_STARTED);
        return true;
    }

    /**
     * SecurityContextHolder 에서 인증된 {@link MemberPrincipal} 을 조회해 MDC {@code memberId} 를 채운다.
     *
     * <p>서비스 도메인 명명을 그대로 사용 (`userId` 가 아니라 `memberId`).
     *
     * <p>Filter 가 아니라 Interceptor 에서 채우는 이유:
     * <ul>
     *     <li>MDC 의 lifecycle (put / clear) 이 Interceptor 한 곳에 모여 누수 위험이 줄어든다.</li>
     *     <li>인증 책임은 Filter, 로그 컨텍스트 책임은 Interceptor 로 분리된다.</li>
     * </ul>
     */
    private void putMemberIdToMdcIfAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }
        if (authentication.getPrincipal() instanceof MemberPrincipal memberPrincipal) {
            MDC.put(MDC_MEMBER_ID, String.valueOf(memberPrincipal.getMemberId()));
        }
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
