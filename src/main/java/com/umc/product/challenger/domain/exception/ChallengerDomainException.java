package com.umc.product.challenger.domain.exception;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;

public class ChallengerDomainException extends BusinessException {
    public ChallengerDomainException(ChallengerErrorCode challengerErrorCode) {
        super(Domain.CHALLENGER, challengerErrorCode);
    }


}
