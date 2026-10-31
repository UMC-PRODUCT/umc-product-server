package com.umc.product.report.domain.enums;

/**
 * 운영자가 신고를 처리하거나 반려할 때 고르는 사유입니다.
 * <p>
 * 처리 사유는 확인된 위반 유형이고, 반려 사유는 위반으로 보지 않은 이유입니다. 각 사유는 한 가지 조치에만 쓸 수 있습니다. 기타 사유는 내용을 알 수
 * 있도록 메모를 함께 남겨야 합니다.
 */
public enum ReportActionReason {

    SPAM(ReportActionType.APPROVE, false),
    ABUSE(ReportActionType.APPROVE, false),
    INAPPROPRIATE(ReportActionType.APPROVE, false),
    PRIVACY(ReportActionType.APPROVE, false),
    OTHER_VIOLATION(ReportActionType.APPROVE, true),

    NOT_VIOLATION(ReportActionType.REJECT, false),
    CONTENT_MISMATCH(ReportActionType.REJECT, false),
    UNVERIFIABLE(ReportActionType.REJECT, false),
    DUPLICATE(ReportActionType.REJECT, false),
    OTHER_REJECTION(ReportActionType.REJECT, true);

    private final ReportActionType actionType;
    private final boolean memoRequired;

    ReportActionReason(ReportActionType actionType, boolean memoRequired) {
        this.actionType = actionType;
        this.memoRequired = memoRequired;
    }

    /**
     * 이 사유를 쓸 수 있는 조치입니다.
     */
    public ReportActionType actionType() {
        return actionType;
    }

    public boolean isMemoRequired() {
        return memoRequired;
    }
}
