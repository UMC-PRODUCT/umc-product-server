package com.umc.product.common.domain.exception;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.CommonErrorCode;
import com.umc.product.global.exception.constant.Domain;

public class CommonException extends BusinessException {
    public CommonException(CommonErrorCode errorCode) {
        super(Domain.COMMON, errorCode);
    }
}
