package com.umc.product.global.exception;


import com.umc.product.global.exception.constant.Domain;
import com.umc.product.global.response.code.BaseCode;
import lombok.Getter;

/**
 * 비즈니스 로직에서 의도적으로 발생시킨 오류를 정의하는 클래스
 */
@Getter
public abstract class BusinessException extends RuntimeException {
    private final Domain domain;
    private final BaseCode baseCode;
    private final String message;

    public BusinessException(Domain domain, BaseCode baseCode, String message) {
        super(message != null ? message : baseCode.getMessage());
        this.domain = domain;
        this.baseCode = baseCode;
        this.message = message;
    }

    public BusinessException(Domain domain, BaseCode baseCode) {
        super(baseCode.getMessage());
        this.domain = domain;
        this.baseCode = baseCode;
        this.message = null;
    }
}
