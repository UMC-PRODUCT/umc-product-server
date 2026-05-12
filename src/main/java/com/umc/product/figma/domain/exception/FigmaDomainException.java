package com.umc.product.figma.domain.exception;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;

public class FigmaDomainException extends BusinessException {
    public FigmaDomainException(FigmaErrorCode errorCode) {
        super(Domain.FIGMA, errorCode);
    }

    public FigmaDomainException(FigmaErrorCode errorCode, String message) {
        super(Domain.FIGMA, errorCode, message);
    }
}
