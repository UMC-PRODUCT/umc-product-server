package com.umc.product.global.event.application.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.umc.product.global.event.application.port.out.LoadOutboxBacklogPort;
import com.umc.product.global.event.application.port.out.LoadOutboxBacklogPort.OutboxBacklog;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OutboxBacklogMetrics {

    private final LoadOutboxBacklogPort port;
    private final OutboxEventRoutingPolicy routingPolicy;
    private final MeterRegistry registry;
    private final Map<OutboxProcessingGroup, Snapshot> snapshots = new ConcurrentHashMap<>();

    public OutboxBacklogMetrics(
        LoadOutboxBacklogPort port, OutboxEventRoutingPolicy routingPolicy, MeterRegistry registry
    ) {
        this.port = port;
        this.routingPolicy = routingPolicy;
        this.registry = registry;
        for (OutboxProcessingGroup group : routingPolicy.groups()) {
            Snapshot snapshot = new Snapshot();
            snapshots.put(group, snapshot);
            Gauge.builder("event.outbox.backlog", snapshot, value -> value.backlog.pending())
                .tags("group", group.name(), "status", "pending").register(registry);
            Gauge.builder("event.outbox.backlog", snapshot, value -> value.backlog.processing())
                .tags("group", group.name(), "status", "processing").register(registry);
            Gauge.builder("event.outbox.oldest.age", snapshot, value -> value.backlog.oldestAgeSeconds())
                .tag("group", group.name()).baseUnit("seconds").register(registry);
            Gauge.builder("event.outbox.backlog.sample.timestamp", snapshot, value -> value.sampledAt)
                .tag("group", group.name()).baseUnit("seconds").register(registry);
        }
    }

    public void refresh() {
        for (OutboxProcessingGroup group : routingPolicy.groups()) {
            try {
                OutboxBacklog backlog = port.getBacklog(routingPolicy.selectionFor(group));
                Snapshot snapshot = snapshots.get(group);
                snapshot.backlog = backlog;
                snapshot.sampledAt = System.currentTimeMillis() / 1000.0;
            } catch (RuntimeException exception) {
                registry.counter("event.outbox.backlog.sample.failure", "group", group.name()).increment();
                log.warn("Outbox 적체 지표 수집 실패: group={}", group.name(), exception);
            }
        }
    }

    private static final class Snapshot {
        private volatile OutboxBacklog backlog = new OutboxBacklog(0, 0, 0);
        private volatile double sampledAt;
    }
}
