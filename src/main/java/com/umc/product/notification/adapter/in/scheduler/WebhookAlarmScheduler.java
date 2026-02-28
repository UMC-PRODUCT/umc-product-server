package com.umc.product.notification.adapter.in.scheduler;

import com.umc.product.notification.application.port.in.FlushWebhookBufferUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.webhook.buffer.enabled", havingValue = "true")
public class WebhookAlarmScheduler {

    private final FlushWebhookBufferUseCase flushWebhookBufferUseCase;

    @Scheduled(fixedRateString = "${app.webhook.buffer.flush-interval-ms:300000}")
    public void flushBufferedAlarms() {
        log.debug("웹훅 알람 버퍼 flush 스케줄 실행");
        flushWebhookBufferUseCase.flush();
    }
}
