package com.umc.product.test.application.service.qa;

import com.umc.product.global.exception.BusinessException;

final class SeedFailureCode {
    private SeedFailureCode() {
    }

    static String of(Throwable throwable) {
        if (throwable instanceof BusinessException business) {
            return business.getBaseCode().getCode();
        }
        return throwable.getClass().getSimpleName();
    }
}
