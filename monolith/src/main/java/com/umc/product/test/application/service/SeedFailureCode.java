package com.umc.product.test.application.service;

import com.umc.product.global.exception.BusinessException;

public final class SeedFailureCode {
    private SeedFailureCode() {
    }

    public static String of(Throwable throwable) {
        if (throwable instanceof BusinessException business) {
            return business.getBaseCode().getCode();
        }
        return throwable.getClass().getSimpleName();
    }
}
