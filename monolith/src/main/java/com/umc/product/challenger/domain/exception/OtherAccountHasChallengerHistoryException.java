package com.umc.product.challenger.domain.exception;

import com.umc.product.global.exception.UserFacingMessage;

public class OtherAccountHasChallengerHistoryException extends ChallengerDomainException implements UserFacingMessage {

    public OtherAccountHasChallengerHistoryException(String guide) {
        super(ChallengerErrorCode.OTHER_ACCOUNT_HAS_CHALLENGER_HISTORY, guide);
    }
}
