package com.umc.product.inquiry.domain.enums;

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
