package com.umc.product.report.domain.enums;

/**
 * 신고 처리 때 대상 콘텐츠를 어떻게 정리했는지 나타냅니다.
 */
public enum ReportTargetRemoval {
    /** 대상을 삭제하지 않았습니다. 반려이거나 삭제 대상이 아닌 신고입니다. */
    NONE,
    /** 이번 처리로 대상을 삭제했습니다. */
    DELETED,
    /** 대상이 이미 삭제되어 있었습니다. */
    ALREADY_DELETED,
    /** 대상을 찾을 수 없어 삭제하지 않았습니다. */
    NOT_FOUND
}
