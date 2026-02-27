package com.umc.product.organization.exception;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.notice.domain.exception.NoticeErrorCode;

public class OrganizationDomainException extends BusinessException {
    public OrganizationDomainException(OrganizationErrorCode organizationErrorCode) {
        super(Domain.ORGANIZATION, organizationErrorCode);
    }
}
