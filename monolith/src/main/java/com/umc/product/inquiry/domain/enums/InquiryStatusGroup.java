package com.umc.product.inquiry.domain.enums;

import java.util.Set;

/**
 * 내 문의 목록의 탭. 진행 중인 문의(OPEN)와 종료된 문의(CLOSED)로 나눈다.
 */
public enum InquiryStatusGroup {
    OPEN(InquiryStatus.OPEN_STATUSES),
    CLOSED(Set.of(InquiryStatus.CLOSED));

    private final Set<InquiryStatus> statuses;

    InquiryStatusGroup(Set<InquiryStatus> statuses) {
        this.statuses = statuses;
    }

    public Set<InquiryStatus> statuses() {
        return statuses;
    }
}
