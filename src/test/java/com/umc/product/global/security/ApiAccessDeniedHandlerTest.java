package com.umc.product.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.global.exception.constant.CommonErrorCode;
import com.umc.product.global.response.ApiErrorResponseWriter;

class ApiAccessDeniedHandlerTest {

    private final ApiAccessDeniedHandler handler =
        new ApiAccessDeniedHandler(new ApiErrorResponseWriter(new ObjectMapper()));

    @Test
    @DisplayName("인가 실패 응답은 내부 AccessDeniedException 메시지를 노출하지 않는다")
    void accessDeniedResponseDoesNotExposeExceptionMessage() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(
            new MockHttpServletRequest(),
            response,
            new AccessDeniedException("ROLE_ADMIN required for internal policy")
        );

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString())
            .contains("\"success\":false")
            .contains("\"code\":\"" + CommonErrorCode.FORBIDDEN.getCode() + "\"")
            .contains("\"message\":\"" + CommonErrorCode.FORBIDDEN.getMessage() + "\"")
            .doesNotContain("ROLE_ADMIN")
            .doesNotContain("internal policy")
            .doesNotContain("\"result\"");
    }
}
