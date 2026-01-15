package com.umc.product.schedule.domain.exception;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;

public class ScheduleDomainException extends BusinessException {

    public ScheduleDomainException(ScheduleErrorCode errorCode) {
        super(Domain.SCHEDULE, errorCode);
    }
}
