package com.umc.product.report.domain.exception;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;

public class ReportDomainException extends BusinessException {

    public ReportDomainException(ReportErrorCode errorCode) {
        super(Domain.REPORT, errorCode);
    }

    public ReportDomainException(ReportErrorCode errorCode, String message) {
        super(Domain.REPORT, errorCode, message);
    }

    public ReportDomainException(ReportErrorCode errorCode, Throwable cause) {
        super(Domain.REPORT, errorCode, cause);
    }
}
