package com.umc.product.maintenance.adapter.in.web.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.global.config.SecurityPathConfig;
import com.umc.product.global.response.ApiResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.maintenance.adapter.in.web.dto.response.MaintenanceWindowResponse;
import com.umc.product.maintenance.application.port.in.query.dto.MaintenanceWindowInfo;
import com.umc.product.maintenance.application.port.out.MaintenanceBypassPolicy;
import com.umc.product.maintenance.application.service.MaintenanceStateHolder;
import com.umc.product.maintenance.domain.MaintenanceDomain;
import com.umc.product.maintenance.domain.MaintenanceScope;
import com.umc.product.maintenance.domain.MaintenanceSnapshot;
import com.umc.product.maintenance.exception.MaintenanceErrorCode;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 점검 활성 시 요청을 503 으로 차단하는 필터.
 * <p>
 * 통과 규칙:
 * <ol>
 *   <li>ALWAYS_ALLOW 경로 (시스템 상태/점검 관리/인증/헬스체크/문서/약관)</li>
 *   <li>스냅샷이 비활성</li>
 *   <li>SUPER_ADMIN 등 bypass 정책 통과</li>
 *   <li>PER_DOMAIN 점검이고 요청 URI 가 대상 도메인이 아님</li>
 * </ol>
 */
@Slf4j
public class MaintenanceFilter extends OncePerRequestFilter {

    private static final List<String> ALWAYS_ALLOW_PATTERNS = SecurityPathConfig.MAINTENANCE_ALWAYS_ALLOW_PATHS;

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final MaintenanceStateHolder stateHolder;
    private final MaintenanceBypassPolicy bypassPolicy;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public MaintenanceFilter(
        MaintenanceStateHolder stateHolder,
        MaintenanceBypassPolicy bypassPolicy,
        ObjectMapper objectMapper
    ) {
        this(stateHolder, bypassPolicy, objectMapper, Clock.systemUTC());
    }

    MaintenanceFilter(
        MaintenanceStateHolder stateHolder,
        MaintenanceBypassPolicy bypassPolicy,
        ObjectMapper objectMapper,
        Clock clock
    ) {
        this.stateHolder = stateHolder;
        this.bypassPolicy = bypassPolicy;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (isAlwaysAllowed(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        MaintenanceSnapshot snapshot = stateHolder.current();
        if (!snapshot.active()) {
            filterChain.doFilter(request, response);
            return;
        }

        Long memberId = resolveMemberId();
        if (memberId != null && bypassPolicy.shouldBypass(memberId)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (snapshot.scope() == MaintenanceScope.PER_DOMAIN && !isTargetedDomain(uri, snapshot)) {
            filterChain.doFilter(request, response);
            return;
        }

        writeMaintenanceResponse(response, snapshot);
    }

    private boolean isAlwaysAllowed(String uri) {
        return ALWAYS_ALLOW_PATTERNS.stream().anyMatch(p -> PATH_MATCHER.match(p, uri));
    }

    private boolean isTargetedDomain(String uri, MaintenanceSnapshot snapshot) {
        return MaintenanceDomain.fromUri(uri)
            .map(snapshot.targetDomains()::contains)
            .orElse(false);
    }

    private Long resolveMemberId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof MemberPrincipal memberPrincipal) {
            return memberPrincipal.getMemberId();
        }
        return null;
    }

    private void writeMaintenanceResponse(HttpServletResponse response, MaintenanceSnapshot snapshot)
        throws IOException {
        MaintenanceErrorCode errorCode = MaintenanceErrorCode.SERVICE_UNDER_MAINTENANCE;
        MaintenanceWindowResponse body = MaintenanceWindowResponse.from(
            new MaintenanceWindowInfo(
                snapshot.activeWindowId(),
                snapshot.scope(),
                snapshot.targetDomains(),
                snapshot.startAt(),
                snapshot.endAt(),
                snapshot.title(),
                snapshot.message(),
                null,
                null,
                null,
                null
            )
        );

        Instant now = clock.instant();
        long retryAfterSeconds = Math.max(1L, Duration.between(now, snapshot.endAt()).getSeconds());

        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));

        ApiResponse<MaintenanceWindowResponse> payload =
            ApiResponse.onFailure(errorCode.getCode(), errorCode.getMessage(), body);

        objectMapper.writeValue(response.getWriter(), payload);
    }
}
