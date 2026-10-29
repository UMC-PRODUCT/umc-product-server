package com.umc.product.report.application.port.in.command;

/**
 * 접수된 신고를 운영 채널로 전달합니다.
 */
public interface DeliverReportUseCase {

    /**
     * 전달 대기 중인 신고를 운영 채널로 보냅니다. 이미 전달했거나 전달 대상이 아니면 아무것도 하지 않습니다.
     * <p>
     * 전달에 실패하면 실패를 기록한 뒤 예외를 다시 던져, 호출한 쪽(outbox relay)이 재시도하게 합니다.
     */
    void deliver(Long reportId);
}
