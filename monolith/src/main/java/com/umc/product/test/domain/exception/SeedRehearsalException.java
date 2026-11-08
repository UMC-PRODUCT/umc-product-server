package com.umc.product.test.domain.exception;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;

public class SeedRehearsalException extends BusinessException {
    public SeedRehearsalException(SeedErrorCode code, String message) {
        super(Domain.PROJECT, code, message);
    }

    public SeedRehearsalException(SeedErrorCode code) {
        super(Domain.PROJECT, code);
    }
}
