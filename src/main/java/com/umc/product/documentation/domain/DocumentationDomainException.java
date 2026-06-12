package com.umc.product.documentation.domain;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;

public class DocumentationDomainException extends BusinessException {

    public DocumentationDomainException(DocumentationErrorCode errorCode, Throwable cause) {
        super(Domain.DOCUMENTATION, errorCode, cause);
    }
}
