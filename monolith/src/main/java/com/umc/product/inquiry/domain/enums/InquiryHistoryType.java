package com.umc.product.inquiry.domain.enums;

/**
 * 운영진이 보는 처리 이력의 종류. {@link #RECEIVED}는 저장하지 않고 문의 접수 시각으로 첫 줄을 만든다.
 */
public enum InquiryHistoryType {
    RECEIVED,
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
