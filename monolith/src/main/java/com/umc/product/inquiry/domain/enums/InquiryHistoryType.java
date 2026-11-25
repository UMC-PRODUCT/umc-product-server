package com.umc.product.inquiry.domain.enums;

/**
 * 저장하는 처리 이력의 종류. DB의 {@code ck_inquiry_history_type}과 같은 값이다.
 */
public enum InquiryHistoryType {
    ASSIGNED,
    REASSIGNED,
    UNASSIGNED,
    TEAM_CHANGED;

    public static InquiryHistoryType ofAssigneeChange(Long previousAssigneeId, Long newAssigneeId) {
        if (previousAssigneeId == null) {
            return ASSIGNED;
        }
        return newAssigneeId == null ? UNASSIGNED : REASSIGNED;
    }
}
