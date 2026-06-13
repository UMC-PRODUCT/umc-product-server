package com.umc.product.global.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import com.umc.product.authorization.domain.exception.AuthorizationDomainException;
import com.umc.product.authorization.domain.exception.AuthorizationErrorCode;
import com.umc.product.global.response.ApiResponse;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;

class CustomErrorControllerTest {

    private final CustomErrorController controller = new CustomErrorController();

    @Test
    @DisplayName("fallback error controller도 BusinessException 상세 메시지를 유지한다")
    void businessExceptionDetailIsPreserved() {
        AuthorizationDomainException exception = new AuthorizationDomainException(
            AuthorizationErrorCode.PERMISSION_DENIED,
            "학교 운영진만 수정할 수 있습니다."
        );

        ResponseEntity<ApiResponse<Object>> response = controller.handleError(errorRequest(exception));

        assertThat(response.getStatusCode()).isEqualTo(AuthorizationErrorCode.PERMISSION_DENIED.getHttpStatus());
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body.getCode()).isEqualTo(AuthorizationErrorCode.PERMISSION_DENIED.getCode());
            assertThat(body.getMessage()).isEqualTo(AuthorizationErrorCode.PERMISSION_DENIED.getMessage());
            assertThat(body.getResult()).isEqualTo("학교 운영진만 수정할 수 있습니다.");
        });
    }

    @Test
    @DisplayName("fallback error controller도 RESOURCE_ACCESS_DENIED 기본 메시지를 detail로 내려준다")
    void resourceAccessDeniedDefaultDetailFallsBackToErrorCodeMessage() {
        AuthorizationDomainException exception =
            new AuthorizationDomainException(AuthorizationErrorCode.RESOURCE_ACCESS_DENIED);

        ResponseEntity<ApiResponse<Object>> response = controller.handleError(errorRequest(exception));

        assertThat(response.getStatusCode()).isEqualTo(AuthorizationErrorCode.RESOURCE_ACCESS_DENIED.getHttpStatus());
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body.getCode()).isEqualTo(AuthorizationErrorCode.RESOURCE_ACCESS_DENIED.getCode());
            assertThat(body.getMessage()).isEqualTo(AuthorizationErrorCode.RESOURCE_ACCESS_DENIED.getMessage());
            assertThat(body.getResult()).isEqualTo(AuthorizationErrorCode.RESOURCE_ACCESS_DENIED.getMessage());
        });
    }

    private MockHttpServletRequest errorRequest(Throwable exception) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, "/error-path");
        request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, new ServletException(exception));
        return request;
    }
}
