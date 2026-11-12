package com.umc.product.global.exception;

import com.umc.product.authorization.domain.exception.AuthorizationDomainException;
import com.umc.product.authorization.domain.exception.AuthorizationErrorCode;
import com.umc.product.global.response.ApiErrorResponseFactory;
import com.umc.product.global.response.ApiResponse;
import com.umc.product.global.response.code.BaseCode;

final class BusinessExceptionResponseResolver {

    private BusinessExceptionResponseResolver() {
    }

    static ApiResponse<Object> toApiResponse(BusinessException exception) {
        BaseCode code = exception.getBaseCode();
        return ApiErrorResponseFactory.from(code, resolveMessage(exception), resolveDetail(exception));
    }

    private static String resolveMessage(BusinessException exception) {
        if (exposesExceptionMessage(exception)) {
            return ApiErrorResponseFactory.resolveMessage(exception.getBaseCode(), exception.getMessage());
        }
        return exception.getBaseCode().getMessage();
    }

    private static Object resolveDetail(BusinessException exception) {
        if (exposesExceptionMessage(exception)) {
            return ApiErrorResponseFactory.resolveMessage(exception.getBaseCode(), exception.getMessage());
        }
        return exception.getMessage();
    }

    private static boolean exposesExceptionMessage(BusinessException exception) {
        if (exception instanceof UserFacingMessage) {
            return true;
        }
        return exception instanceof AuthorizationDomainException
            && exception.getBaseCode().equals(AuthorizationErrorCode.RESOURCE_ACCESS_DENIED);
    }
}
