package com.umc.product.inquiry.domain.enums;

import java.util.Set;

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
