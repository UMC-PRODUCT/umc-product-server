package com.umc.product.notification.domain.exception;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;

public class WebhookDomainException extends BusinessException {
    public WebhookDomainException(WebhookErrorCode webhookErrorCode) {
        super(Domain.WEBHOOK, webhookErrorCode);
    }
}
