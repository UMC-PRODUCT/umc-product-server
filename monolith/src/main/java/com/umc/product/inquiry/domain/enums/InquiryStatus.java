package com.umc.product.inquiry.domain.enums;

import java.util.Set;

/**
 * 문의 상태와 메시지에 따른 전이 표.
 * <pre>
 * 현재 상태      | 운영진 메시지 | 작성자 메시지 | 종료
 * RECEIVED      | IN_PROGRESS  | RECEIVED     | CLOSED
 * IN_PROGRESS   | IN_PROGRESS  | IN_PROGRESS  | CLOSED
 * CLOSED        | IN_PROGRESS  | IN_PROGRESS  | 불가
 * </pre>
 */
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
