package com.umc.product.report.domain.enums;

/**
 * 신고에 대한 운영자 조치입니다.
 * <p>
 * 메시지 삭제나 계정 정지 같은 실제 제재가 정해지면 여기에 조치를 추가합니다.
 */
public enum ReportActionType {

    /** 운영 정책 위반이 확인되어 처리합니다. 실제 제재는 별도 조치로 다룹니다. */
    APPROVE(ReportStatus.APPROVED),

    /** 위반이 아니거나 위반 사실을 확인하기 어려워 반려합니다. */
    REJECT(ReportStatus.REJECTED);

    private final ReportStatus resultStatus;

    ReportActionType(ReportStatus resultStatus) {
        this.resultStatus = resultStatus;
    }

    /**
     * 이 조치를 하면 신고가 바뀌는 처리 상태입니다.
     */
    public ReportStatus resultStatus() {
        return resultStatus;
    }
}
