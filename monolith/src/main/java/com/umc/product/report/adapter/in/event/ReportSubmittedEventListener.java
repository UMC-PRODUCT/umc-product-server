package com.umc.product.report.adapter.in.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.umc.product.report.application.port.in.command.DeliverReportUseCase;
import com.umc.product.report.domain.event.ReportSubmittedEvent;

import lombok.RequiredArgsConstructor;

/**
 * outbox relay가 다시 발행한 신고 접수 이벤트를 받아 운영 채널로 전달합니다.
 * <p>
 * 예외가 relay까지 올라가야 재시도되므로 비동기로 처리하지 않습니다.
 */
@Component
@RequiredArgsConstructor
public class ReportSubmittedEventListener {

    private final DeliverReportUseCase deliverReportUseCase;

    @EventListener
    public void handle(ReportSubmittedEvent event) {
        deliverReportUseCase.deliver(event.reportId());
    }
}
