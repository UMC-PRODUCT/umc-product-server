package com.umc.product.notification.adapter.in.scheduler;

import com.umc.product.notification.application.port.in.ProcessFcmOutboxUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmOutboxScheduler {

    private final ProcessFcmOutboxUseCase processFcmOutboxUseCase;

    @Scheduled(fixedRateString = "${app.fcm.outbox.interval-ms:30000}")
    public void processPendingEvents() {
        log.debug("FCM outbox 처리 스케줄 실행");
        processFcmOutboxUseCase.process();
    }
}
