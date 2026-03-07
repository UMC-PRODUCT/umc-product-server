package com.umc.product.organization.domain;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.organization.exception.OrganizationErrorCode;

public class OrganizationDomainException extends BusinessException {
    public OrganizationDomainException(OrganizationErrorCode errorCode) {
        super(Domain.ORGANIZATION, errorCode);
    }

    public OrganizationDomainException(OrganizationErrorCode errorCode, String message) {
        super(Domain.ORGANIZATION, errorCode, message);
    }
}
