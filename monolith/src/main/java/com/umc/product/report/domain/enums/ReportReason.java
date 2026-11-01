package com.umc.product.report.domain.enums;

import java.util.Set;

/**
 * 신고 사유입니다. 신고 대상 종류마다 고를 수 있는 사유가 다릅니다.
 */
public enum ReportReason {
    SPAM,
    ABUSE,
    INAPPROPRIATE,
    PRIVACY,
    HARASSMENT,
    IMPERSONATION,
    ETC;

    private static final Set<ReportReason> USER_REASONS = Set.of(HARASSMENT, IMPERSONATION, SPAM, ETC);
    private static final Set<ReportReason> THREAD_MESSAGE_REASONS = Set.of(SPAM, ABUSE, INAPPROPRIATE, PRIVACY, ETC);

    /**
     * 해당 대상 종류를 신고할 때 쓸 수 있는 사유인지 확인합니다. 게시글·댓글은 사유를 받지 않으므로 쓸 수 없습니다.
     */
    public boolean isAllowedFor(ReportTargetType targetType) {
        return switch (targetType) {
            case USER -> USER_REASONS.contains(this);
            case THREAD_MESSAGE -> THREAD_MESSAGE_REASONS.contains(this);
            case POST, COMMENT -> false;
        };
    }
}
