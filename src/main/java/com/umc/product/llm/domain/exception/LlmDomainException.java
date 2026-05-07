package com.umc.product.llm.domain.exception;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;

public class LlmDomainException extends BusinessException {
    public LlmDomainException(LlmErrorCode errorCode) {
        super(Domain.LLM, errorCode);
    }

    public LlmDomainException(LlmErrorCode errorCode, String message) {
        super(Domain.LLM, errorCode, message);
    }
}
