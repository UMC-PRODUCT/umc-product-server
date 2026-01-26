package com.umc.product.global.exception;


import com.umc.product.global.exception.constant.Domain;
import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 비즈니스 로직에서 의도적으로 발생시킨 오류를 정의하는 클래스
 */
@Getter
@AllArgsConstructor
public class BusinessException extends RuntimeException {
    private Domain domain;
    private BaseCode code;
    // TODO: baseCode로 변경 .. getCode().getCode() 처럼 써야 하는 경우가 발생함
    private String message;

    public BusinessException(Domain domain, BaseCode code) {
        this.domain = domain;
        this.code = code;
        this.message = null;
    }
}
