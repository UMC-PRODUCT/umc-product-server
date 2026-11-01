package com.umc.product.global.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;

import com.umc.product.authorization.domain.exception.AuthorizationDomainException;
import com.umc.product.authorization.domain.exception.AuthorizationErrorCode;
import com.umc.product.global.exception.constant.CommonErrorCode;
import com.umc.product.global.response.ApiResponse;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("RESOURCE_ACCESS_DENIED 기본 예외도 message가 null로 내려가지 않는다")
    void resourceAccessDeniedDefaultMessageFallback() {
        AuthorizationDomainException exception =
            new AuthorizationDomainException(AuthorizationErrorCode.RESOURCE_ACCESS_DENIED);

        ResponseEntity<Object> response = handler.onThrowException(
            exception,
            new ServletWebRequest(new MockHttpServletRequest())
        );

        assertThat(response.getStatusCode()).isEqualTo(AuthorizationErrorCode.RESOURCE_ACCESS_DENIED.getHttpStatus());
        assertThat(response.getBody())
            .isInstanceOfSatisfying(ApiResponse.class, body -> {
                assertThat(body.getCode()).isEqualTo(AuthorizationErrorCode.RESOURCE_ACCESS_DENIED.getCode());
                assertThat(body.getMessage()).isEqualTo(AuthorizationErrorCode.RESOURCE_ACCESS_DENIED.getMessage());
                assertThat(body.getResult()).isEqualTo(AuthorizationErrorCode.RESOURCE_ACCESS_DENIED.getMessage());
            });
    }

    @Test
    @DisplayName("Spring Security AccessDeniedException은 MVC 경로에서도 403으로 응답한다")
    void accessDeniedExceptionReturnsForbidden() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new ExceptionController())
            .setControllerAdvice(handler)
            .setMessageConverters(new MappingJackson2HttpMessageConverter())
            .build();

        mockMvc.perform(get("/access-denied"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.getCode()))
            .andExpect(jsonPath("$.message").value(CommonErrorCode.FORBIDDEN.getMessage()))
            .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("JSON 파싱 오류는 내부 파서 상세 메시지를 응답에 노출하지 않는다")
    void jsonParseErrorDoesNotExposeParserDetail() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new ExceptionController())
            .setControllerAdvice(handler)
            .setMessageConverters(new MappingJackson2HttpMessageConverter())
            .build();

        MvcResult result = mockMvc.perform(post("/body")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("COMMON-400"))
            .andExpect(jsonPath("$.result").value("요청 형식이 올바르지 않아요. 입력한 값을 확인해주세요."))
            .andReturn();

        assertThat(result.getResponse().getContentAsString())
            .doesNotContain("JsonParseException")
            .doesNotContain("Unexpected")
            .doesNotContain("com.fasterxml");
    }

    @Test
    @DisplayName("요청 본문이 없으면 사용자가 이해할 수 있는 다음 행동을 안내한다")
    void missingRequestBodyReturnsActionableMessage() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new ExceptionController())
            .setControllerAdvice(handler)
            .setMessageConverters(new MappingJackson2HttpMessageConverter())
            .build();

        mockMvc.perform(post("/body")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value(CommonErrorCode.BAD_REQUEST.getCode()))
            .andExpect(jsonPath("$.result").value("요청 내용이 비어 있어요. 입력한 값을 확인해주세요."));
    }

    @Test
    @DisplayName("요청 값 형식이 맞지 않으면 사용자가 확인할 값을 안내한다")
    void requestTypeMismatchReturnsActionableMessage() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new ExceptionController())
            .setControllerAdvice(handler)
            .setMessageConverters(new MappingJackson2HttpMessageConverter())
            .build();

        mockMvc.perform(get("/number").param("value", "not-number"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value(CommonErrorCode.BAD_REQUEST.getCode()))
            .andExpect(jsonPath("$.result").value("요청 값의 형식이 올바르지 않아요. 입력한 값을 확인해주세요."));
    }

    @RestController
    private static class ExceptionController {

        @GetMapping("/access-denied")
        String accessDenied() {
            throw new AccessDeniedException("ROLE_ADMIN required for internal policy");
        }

        @PostMapping("/body")
        String body(@RequestBody TestRequest request) {
            return request.name();
        }

        @GetMapping("/number")
        String number(@RequestParam Integer value) {
            return value.toString();
        }
    }

    private record TestRequest(String name) {
    }
}
