package com.umc.product.terms.domain.exception;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;

public class TermsDomainException extends BusinessException {
    public TermsDomainException(TermsErrorCode termsErrorCode) {
        super(Domain.TERMS, termsErrorCode);
    }
}
