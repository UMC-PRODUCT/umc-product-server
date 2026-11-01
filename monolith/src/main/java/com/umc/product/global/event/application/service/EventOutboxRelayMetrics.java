package com.umc.product.global.event.application.service;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.umc.product.global.event.domain.EventOutboxStatus;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@Component
public class EventOutboxRelayMetrics {

    private final MeterRegistry registry;
    private final Counter retryCounter;
    private final Counter failedCounter;

    public EventOutboxRelayMetrics(MeterRegistry meterRegistry) {
        registry = meterRegistry;
        retryCounter = Counter.builder("event.outbox.relay.retry")
            .description("Event outbox publish failures scheduled for retry")
            .register(meterRegistry);
        failedCounter = Counter.builder("event.outbox.relay.failed")
            .description("Event outbox publish failures exhausted retries")
            .register(meterRegistry);
    }

    static EventOutboxRelayMetrics noOp() {
        return new EventOutboxRelayMetrics(new SimpleMeterRegistry());
    }

    public void recordDuration(String stage, String group, String outcome, long startedAt) {
        Timer.builder("event.outbox." + stage + ".duration")
            .tags("group", group, "outcome", outcome)
            .register(registry).record(System.nanoTime() - startedAt, TimeUnit.NANOSECONDS);
    }

    public void recordRejection(String group) {
        registry.counter("event.outbox.worker.rejected", "group", group).increment();
    }

    public void recordGroupFailure(String group, EventOutboxStatus status) {
        if (status == EventOutboxStatus.PENDING || status == EventOutboxStatus.FAILED) {
            registry.counter("event.outbox.group.failure", "group", group,
                "outcome", status == EventOutboxStatus.PENDING ? "retry" : "failed").increment();
        }
    }

    public void recordFailure(EventOutboxStatus status) {
        switch (status) {
            case PENDING -> retryCounter.increment();
            case FAILED -> failedCounter.increment();
            case PROCESSING, PUBLISHED -> {
            }
        }
    }
}
