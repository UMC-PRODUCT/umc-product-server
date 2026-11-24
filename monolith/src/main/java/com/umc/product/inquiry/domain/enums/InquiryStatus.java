package com.umc.product.inquiry.domain.enums;

import java.util.Set;

public enum InquiryStatus {
    RECEIVED,
    IN_PROGRESS,
    CLOSED;

    public static final Set<InquiryStatus> OPEN_STATUSES = Set.of(RECEIVED, IN_PROGRESS);

    public InquiryStatus afterMessageFrom(InquiryParticipant sender) {
        return switch (this) {
            case RECEIVED -> sender == InquiryParticipant.STAFF ? IN_PROGRESS : RECEIVED;
            case IN_PROGRESS, CLOSED -> IN_PROGRESS;
        };
    }

    public boolean isClosable() {
        return this != CLOSED;
    }
}
