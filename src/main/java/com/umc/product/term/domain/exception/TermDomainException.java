package com.umc.product.term.domain.exception;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;

public class TermDomainException extends BusinessException {
    public TermDomainException(TermErrorCode termErrorCode) {
        super(Domain.TERMS, termErrorCode);
    }

    public TermDomainException(TermErrorCode termErrorCode, String message) {
        super(Domain.TERMS, termErrorCode, message);
    }
}
