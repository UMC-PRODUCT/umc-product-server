package com.umc.product.challenger.domain.exception;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;

public class ChallengerDomainException extends BusinessException {
    public ChallengerDomainException(ChallengerErrorCode challengerErrorCode) {
        super(Domain.CHALLENGER, challengerErrorCode);
    }

    public ChallengerDomainException(ChallengerErrorCode challengerErrorCode, String message) {
        super(Domain.CHALLENGER, challengerErrorCode, message);
    }
}
