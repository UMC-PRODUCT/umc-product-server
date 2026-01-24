package com.umc.product.authorization.domain.exception;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;

public class AuthorizationDomainException extends BusinessException {

    public AuthorizationDomainException(AuthorizationErrorCode errorCode) {
        super(Domain.AUTHORIZATION, errorCode);
    }

    public AuthorizationDomainException(AuthorizationErrorCode errorCode, String message) {
        super(Domain.AUTHORIZATION, errorCode, message);
    }
}
