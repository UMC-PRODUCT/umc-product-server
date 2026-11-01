package com.umc.product.global.event.adapter.in.scheduler;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.umc.product.global.event.application.port.in.RequestOutboxRunUseCase;

@Component
@ConditionalOnProperty(
    prefix = "app.event-outbox",
    name = "relay-enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class EventOutboxPoller {

    private final RequestOutboxRunUseCase realtimeWorker;
    private final RequestOutboxRunUseCase backgroundWorker;

    public EventOutboxPoller(
        @Qualifier("realtimeOutboxWorker") RequestOutboxRunUseCase realtimeWorker,
        @Qualifier("backgroundOutboxWorker") RequestOutboxRunUseCase backgroundWorker
    ) {
        this.realtimeWorker = realtimeWorker;
        this.backgroundWorker = backgroundWorker;
    }

    @Scheduled(fixedDelayString =
        "${app.event-outbox.workers.realtime.poll-interval-ms:${app.event-outbox.poll-interval-ms:1000}}")
    public void pollRealtime() {
        realtimeWorker.requestRun();
    }

    @Scheduled(fixedDelayString =
        "${app.event-outbox.workers.background.poll-interval-ms:${app.event-outbox.poll-interval-ms:1000}}")
    public void pollBackground() {
        backgroundWorker.requestRun();
    }
}
