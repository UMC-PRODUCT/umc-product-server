package com.umc.product.global.event.adapter.in.event;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.umc.product.global.event.application.event.RealtimeOutboxCommittedEvent;
import com.umc.product.global.event.application.port.in.RequestOutboxRunUseCase;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(
    prefix = "app.event-outbox", name = "relay-enabled", havingValue = "true", matchIfMissing = true
)
@ConditionalOnProperty(
    prefix = "app.event-outbox.workers.realtime", name = "immediate-dispatch-enabled", havingValue = "true"
)
public class RealtimeOutboxCommitListener {

    private final RequestOutboxRunUseCase realtimeWorker;

    public RealtimeOutboxCommitListener(
        @Qualifier("realtimeOutboxWorker") RequestOutboxRunUseCase realtimeWorker
    ) {
        this.realtimeWorker = realtimeWorker;
    }

    @EventListener
    public void onCommitted(RealtimeOutboxCommittedEvent event) {
        // 이미 커밋된 신호다. 요청 스레드에서는 실행만 요청하고 DB 처리는 기존 Worker에 맡긴다.
        try {
            if (!realtimeWorker.requestRun()) {
                log.warn("실시간 Outbox 실행 요청 미수락. 복구 폴링에서 재시도합니다.");
            }
        } catch (RuntimeException exception) {
            log.warn("실시간 Outbox 실행 요청 실패. 복구 폴링에서 재시도합니다.");
        }
    }
}
