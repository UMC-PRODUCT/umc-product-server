package com.umc.product.notification.adapter.in.event;

import com.umc.product.notification.application.port.in.ProcessFcmOutboxUseCase;
import com.umc.product.notification.domain.FcmOutboxEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmOutboxEventListener {

    private final ProcessFcmOutboxUseCase processFcmOutboxUseCase;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFcmOutboxEvent(FcmOutboxEvent event) {
        log.debug("FCM outbox 즉시 처리 시작");
        processFcmOutboxUseCase.process();
    }
}
