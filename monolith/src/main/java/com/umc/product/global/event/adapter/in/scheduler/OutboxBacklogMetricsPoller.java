package com.umc.product.global.event.adapter.in.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.umc.product.global.event.application.service.OutboxBacklogMetrics;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.event-outbox.metrics", name = "enabled", havingValue = "true",
    matchIfMissing = true)
public class OutboxBacklogMetricsPoller {

    private final OutboxBacklogMetrics metrics;

    @Scheduled(fixedDelayString = "${app.event-outbox.metrics.sample-interval-ms:30000}")
    public void sample() {
        metrics.refresh();
    }
}
