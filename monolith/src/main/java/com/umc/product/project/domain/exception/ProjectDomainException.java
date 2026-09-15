package com.umc.product.project.domain.exception;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;

public class ProjectDomainException extends BusinessException {
    public ProjectDomainException(ProjectErrorCode projectErrorCode) {
        super(Domain.PROJECT, projectErrorCode);
    }

    public ProjectDomainException(ProjectErrorCode projectErrorCode, String message) {
        super(Domain.PROJECT, projectErrorCode, message);
    }
}
