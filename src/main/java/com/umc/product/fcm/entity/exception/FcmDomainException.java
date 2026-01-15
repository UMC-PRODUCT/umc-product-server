package com.umc.product.fcm.entity.exception;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;

public class FcmDomainException extends BusinessException {
    public FcmDomainException(FcmErrorCode fcmErrorCode) {
        super(Domain.FCM, fcmErrorCode);
    }


}
