package com.umc.product.report.application.port.out;

import com.umc.product.report.domain.enums.ReportTargetRemoval;

/**
 * 신고 처리(승인) 때 신고 대상 콘텐츠를 삭제하는 포트입니다. report는 대상 도메인을 모르므로 구현은 대상 도메인이 제공합니다.
 */
public interface RemoveReportTargetPort {

    /**
     * 스레드 메시지를 삭제합니다. 신고 처리와 같은 트랜잭션에서 호출되며, 예외가 나면 신고 처리 전체가 롤백됩니다.
     *
     * @return 삭제 결과. {@link ReportTargetRemoval#NONE}은 돌려주지 않습니다.
     */
    ReportTargetRemoval removeThreadMessage(Long threadId, Long messageId);
}
