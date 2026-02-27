package com.umc.product.notification.domain.exception;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;

public class EmailDomainException extends BusinessException {

    public EmailDomainException(EmailErrorCode emailErrorCode) {
        super(Domain.EMAIL, emailErrorCode);
    }
}
