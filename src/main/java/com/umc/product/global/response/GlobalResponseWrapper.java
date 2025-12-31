package com.umc.product.global.response;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class GlobalResponseWrapper implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        // 이미 ApiResponse로 래핑된 경우 제외
        if (returnType.getParameterType().equals(ApiResponse.class)) {
            return false;
        }

        // Swagger/OpenAPI 관련 컨트롤러 제외
        String className = returnType.getContainingClass().getName();
        if (className.startsWith("org.springdoc") ||
                className.startsWith("org.springframework")) {
            return false;
        }

        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        // String 타입은 별도 처리 필요 (MessageConverter 이슈)
        //
        if (body instanceof String) {
            return body;
        }

        return ApiResponse.onSuccess(body);
    }
}