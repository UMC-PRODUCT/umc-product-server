package com.umc.product.inquiry.domain.exception;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;

public class InquiryDomainException extends BusinessException {

    public InquiryDomainException(InquiryErrorCode errorCode) {
        super(Domain.INQUIRY, errorCode);
    }

    public InquiryDomainException(InquiryErrorCode errorCode, Throwable cause) {
        super(Domain.INQUIRY, errorCode, cause);
    }
}
