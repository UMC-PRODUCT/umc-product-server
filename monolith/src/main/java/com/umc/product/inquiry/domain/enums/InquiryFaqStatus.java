package com.umc.product.inquiry.domain.enums;

import com.umc.product.inquiry.domain.exception.InquiryDomainException;
import com.umc.product.inquiry.domain.exception.InquiryErrorCode;

public enum InquiryFaqStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVED;

    public InquiryFaqStatus publish() {
        return PUBLISHED;
    }

    public InquiryFaqStatus archive() {
        if (this != PUBLISHED) {
            throw new InquiryDomainException(InquiryErrorCode.INQUIRY_FAQ_NOT_ARCHIVABLE);
        }
        return ARCHIVED;
    }
}
