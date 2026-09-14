package com.umc.product.maintenance.exception;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;

public class MaintenanceDomainException extends BusinessException {

    public MaintenanceDomainException(MaintenanceErrorCode errorCode) {
        super(Domain.MAINTENANCE, errorCode);
    }

    public MaintenanceDomainException(MaintenanceErrorCode errorCode, String message) {
        super(Domain.MAINTENANCE, errorCode, message);
    }
}
