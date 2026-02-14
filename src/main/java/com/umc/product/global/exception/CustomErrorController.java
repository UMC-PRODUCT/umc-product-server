package com.umc.product.global.exception;

import com.umc.product.global.exception.constant.CommonErrorCode;
import com.umc.product.global.response.ApiResponse;
import com.umc.product.global.response.code.BaseCode;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * GlobalExceptionHandler가 처리하지 못하는 에러를 처리하는 fallback 컨트롤러 - 404 Not Found (존재하지 않는 URL) - Filter에서 발생한 예외 - Servlet 레벨
 * 에러
 */
@Hidden
@Slf4j
@RestController
@RequestMapping("/error")
public class CustomErrorController implements ErrorController {

    @RequestMapping
    public ResponseEntity<ApiResponse<Object>> handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Throwable exception = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        // BusinessException인 경우 해당 에러코드 사용
        if (exception != null) {
            Throwable cause = getRootCause(exception);
            if (cause instanceof BusinessException businessException) {
                BaseCode code = businessException.getCode();
                log.warn("[ERROR CONTROLLER - BusinessException] uri={}, code={}, message={}",
                    requestUri, code.getCode(), code.getMessage());

                return ResponseEntity
                    .status(code.getHttpStatus())
                    .body(ApiResponse.onFailure(code.getCode(), code.getMessage(), null));
            }
        }

        // 기본 처리
        HttpStatus status = HttpStatus.resolve(statusCode != null ? statusCode : 500);
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        log.warn("[ERROR CONTROLLER] status={}, uri={}", statusCode, requestUri);

        CommonErrorCode errorCode = resolveErrorCode(status);

        ApiResponse<Object> body = ApiResponse.onFailure(
            errorCode.getCode(),
            errorCode.getMessage(),
            null
        );

        return ResponseEntity.status(status).body(body);
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }

    private CommonErrorCode resolveErrorCode(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> CommonErrorCode.NOT_FOUND;
            case BAD_REQUEST -> CommonErrorCode.BAD_REQUEST;
            case UNAUTHORIZED -> CommonErrorCode.UNAUTHORIZED;
            case FORBIDDEN -> CommonErrorCode.FORBIDDEN;
            default -> CommonErrorCode.INTERNAL_SERVER_ERROR;
        };
    }
}
