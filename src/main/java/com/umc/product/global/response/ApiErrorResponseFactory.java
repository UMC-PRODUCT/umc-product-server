package com.umc.product.global.response;

import org.springframework.util.StringUtils;

import com.umc.product.global.response.code.BaseCode;

public final class ApiErrorResponseFactory {

    private ApiErrorResponseFactory() {
    }

    public static ApiResponse<Object> from(BaseCode code) {
        return from(code, null);
    }

    public static ApiResponse<Object> from(BaseCode code, Object detail) {
        return from(code, code.getMessage(), detail);
    }

    public static ApiResponse<Object> from(BaseCode code, String message, Object detail) {
        return ApiResponse.onFailure(code.getCode(), resolveMessage(code, message), detail);
    }

    public static String resolveMessage(BaseCode code, String message) {
        if (StringUtils.hasText(message)) {
            return message;
        }
        return code.getMessage();
    }
}
