package com.umc.product.inquiry.domain.enums;

import com.umc.product.inquiry.domain.exception.InquiryDomainException;
import com.umc.product.inquiry.domain.exception.InquiryErrorCode;

/**
 * 대표 질문 상태. 어느 상태에서든 수정본을 발행할 수 있고, 보관은 발행 중일 때만 한다.
 *
 * <pre>
 * DRAFT ──publish──▶ PUBLISHED ──archive──▶ ARCHIVED
 *                      ▲    │publish(수정본)         │
 *                      └────┘◀──────publish───────────┘
 * </pre>
 */
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
