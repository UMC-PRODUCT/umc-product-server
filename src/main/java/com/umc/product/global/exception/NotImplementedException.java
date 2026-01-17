package com.umc.product.global.exception;

import com.umc.product.global.exception.constant.CommonErrorCode;
import com.umc.product.global.exception.constant.Domain;

/**
 * Controller에서 설계만 완료되었고, 아직 구현되지 않은 경우 해당 에러를 발생시켜야 합니다.
 */
public class NotImplementedException extends BusinessException {
    public NotImplementedException() {
        super(Domain.COMMON, CommonErrorCode.NOT_IMPLEMENTED);
    }
}
