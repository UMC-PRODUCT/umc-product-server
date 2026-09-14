package com.umc.product.feedback.domain.exception;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;

public class FeedbackDomainException extends BusinessException {
    public FeedbackDomainException(FeedbackErrorCode feedbackErrorCode) {
        super(Domain.FEEDBACK, feedbackErrorCode);
    }

    public FeedbackDomainException(FeedbackErrorCode feedbackErrorCode, String message) {
        super(Domain.FEEDBACK, feedbackErrorCode, message);
    }
}
