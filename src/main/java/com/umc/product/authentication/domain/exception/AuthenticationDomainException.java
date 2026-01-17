package com.umc.product.authentication.domain.exception;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;

public class AuthenticationDomainException extends BusinessException {
    public AuthenticationDomainException(AuthenticationErrorCode errorCode) {
        super(Domain.AUTH, errorCode);
    }
}
