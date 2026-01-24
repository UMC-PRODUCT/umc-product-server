package com.umc.product.global.security;

import static com.umc.product.global.security.JwtAuthenticationFilter.JWT_ERROR_ATTRIBUTE;
import static com.umc.product.global.security.JwtAuthenticationFilter.JWT_UNKNOWN_ERROR_ATTRIBUTE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.global.exception.constant.CommonErrorCode;
import com.umc.product.global.response.ApiResponse;
import com.umc.product.global.response.code.BaseCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex)
            throws IOException {

        BaseCode errorCode = resolveErrorCode(req, ex);

        log.warn("[AUTHENTICATION FAILED] URI: {}, ErrorCode: {}, Message: {}",
                req.getRequestURI(),
                errorCode.getCode(),
                errorCode.getMessage(),
                ex);

        ApiResponse<Object> body = ApiResponse.onFailure(
                errorCode.getCode(),
                errorCode.getMessage(),
                null
        );

        res.setStatus(errorCode.getHttpStatus().value());
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private BaseCode resolveErrorCode(HttpServletRequest req, AuthenticationException ex) {
        // 1. JwtAuthenticationFilter에서 저장한 JWT 에러 확인
        Object jwtError = req.getAttribute(JWT_ERROR_ATTRIBUTE);
        if (jwtError instanceof AuthenticationDomainException domainException) {
            return domainException.getCode();
        }

        // 2. 알 수 없는 JWT 에러
        Object unknownError = req.getAttribute(JWT_UNKNOWN_ERROR_ATTRIBUTE);
        if (unknownError != null) {
            log.error("[JWT UNKNOWN ERROR]", (Throwable) unknownError);
            return CommonErrorCode.INTERNAL_SERVER_ERROR;
        }

        // 3. cause에서 확인 (fallback)
        Throwable cause = ex.getCause();
        if (cause instanceof AuthenticationDomainException domainException) {
            return domainException.getCode();
        }

        // 4. 기본값: 인증 정보 없음
        return CommonErrorCode.SECURITY_NOT_GIVEN;
    }
}
