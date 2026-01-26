package com.umc.product.storage.domain.exception;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;

public class StorageException extends BusinessException {

    public StorageException(StorageErrorCode errorCode) {
        super(Domain.STORAGE, errorCode);
    }
}
