package com.umc.product.term.adapter.in.web.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.UrlPathHelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.global.response.ApiResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.util.PublicEndpointCollector;
import com.umc.product.global.security.util.SecurityEndpoint;
import com.umc.product.global.security.util.SecurityEndpointAllowlist;
import com.umc.product.term.domain.exception.TermErrorCode;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class TermConsentEnforcementFilter extends OncePerRequestFilter {

    private static final UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();

    private final ObjectMapper objectMapper;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private volatile List<SecurityEndpoint> allowedEndpoints;

    public TermConsentEnforcementFilter(
        ObjectMapper objectMapper,
        RequestMappingHandlerMapping requestMappingHandlerMapping
    ) {
        this.objectMapper = objectMapper;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    TermConsentEnforcementFilter(
        ObjectMapper objectMapper,
        List<SecurityEndpoint> publicEndpoints
    ) {
        this.objectMapper = objectMapper;
        this.requestMappingHandlerMapping = null;
        this.allowedEndpoints = SecurityEndpointAllowlist.reconsentBypassEndpoints(publicEndpoints);
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        if (isAllowedEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        MemberPrincipal memberPrincipal = resolveMemberPrincipal();
        if (memberPrincipal == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (memberPrincipal.isRequiredTermsAgreed()) {
            filterChain.doFilter(request, response);
            return;
        }

        writeReconsentRequiredResponse(response);
    }

    private boolean isAllowedEndpoint(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = URL_PATH_HELPER.getPathWithinApplication(request);

        return allowedEndpoints().stream().anyMatch(endpoint -> endpoint.matches(method, uri));
    }

    private List<SecurityEndpoint> allowedEndpoints() {
        List<SecurityEndpoint> endpoints = allowedEndpoints;
        if (endpoints != null) {
            return endpoints;
        }

        synchronized (this) {
            if (allowedEndpoints == null) {
                List<SecurityEndpoint> collectedEndpoints =
                    PublicEndpointCollector.collectPublicEndpoints(requestMappingHandlerMapping);
                allowedEndpoints = SecurityEndpointAllowlist.reconsentBypassEndpoints(collectedEndpoints);
            }
            return allowedEndpoints;
        }
    }

    private MemberPrincipal resolveMemberPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof MemberPrincipal memberPrincipal) {
            return memberPrincipal;
        }

        return null;
    }

    private void writeReconsentRequiredResponse(HttpServletResponse response) throws IOException {
        TermErrorCode errorCode = TermErrorCode.TERMS_RECONSENT_REQUIRED;

        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ApiResponse<Void> payload = ApiResponse.onFailure(
            errorCode.getCode(),
            errorCode.getMessage(),
            null
        );

        objectMapper.writeValue(response.getWriter(), payload);
    }

}
