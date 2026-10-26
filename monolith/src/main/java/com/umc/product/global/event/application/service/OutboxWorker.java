package com.umc.product.global.event.application.service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.IntStream;

import com.umc.product.global.event.application.port.in.RequestOutboxRunUseCase;
import com.umc.product.global.event.application.port.out.OutboxEventSelection;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class OutboxWorker implements RequestOutboxRunUseCase, AutoCloseable {

    private final OutboxProcessingGroup group;
    private final Executor executor;
    private final EventOutboxRelayService relay;
    private final OutboxEventSelection selection;
    private final int batchSize;
    private final EventOutboxRelayMetrics metrics;
    private final List<ExecutionSlot> slots;
    private boolean closed;

    public OutboxWorker(
        OutboxProcessingGroup group,
        Executor executor,
        EventOutboxRelayService relay,
        OutboxEventRoutingPolicy routingPolicy,
        int batchSize
    ) {
        this(group, executor, relay, routingPolicy, batchSize, EventOutboxRelayMetrics.noOp());
    }

    public OutboxWorker(
        OutboxProcessingGroup group,
        Executor executor,
        EventOutboxRelayService relay,
        OutboxEventRoutingPolicy routingPolicy,
        int batchSize,
        EventOutboxRelayMetrics metrics
    ) {
        this(group, executor, relay, routingPolicy, batchSize, metrics, 1);
    }

    public OutboxWorker(
        OutboxProcessingGroup group,
        Executor executor,
        EventOutboxRelayService relay,
        OutboxEventRoutingPolicy routingPolicy,
        int batchSize,
        EventOutboxRelayMetrics metrics,
        int concurrency
    ) {
        this.metrics = Objects.requireNonNull(metrics);
        this.group = Objects.requireNonNull(group);
        this.executor = Objects.requireNonNull(executor);
        this.relay = Objects.requireNonNull(relay);
        this.selection = routingPolicy.selectionFor(group);
        if (batchSize <= 0) {
            throw new IllegalArgumentException("배치 크기는 양수여야 합니다.");
        }
        this.batchSize = batchSize;
        if (concurrency <= 0) {
            throw new IllegalArgumentException("동시 실행 수는 양수여야 합니다.");
        }
        slots = IntStream.range(0, concurrency).mapToObj(index -> new ExecutionSlot()).toList();
    }

    // 슬롯마다 실행 요청을 하나의 후속 신호로 합쳐 executor 적재량을 제한한다.
    @Override
    public synchronized boolean requestRun() {
        if (closed) {
            return false;
        }
        boolean accepted = false;
        for (ExecutionSlot slot : slots) {
            slot.pending = true;
            if (slot.scheduled) {
                accepted = true;
                continue;
            }
            slot.scheduled = true;
            try {
                executor.execute(() -> drain(slot));
                accepted = true;
            } catch (RejectedExecutionException exception) {
                slot.scheduled = false;
                slot.pending = false;
                metrics.recordRejection(group.name());
                log.warn("Outbox worker 실행 요청 거절: group={}", group.name());
            }
        }
        return accepted;
    }

    private void drain(ExecutionSlot slot) {
        try {
            while (takeSignal(slot)) {
                try {
                    relay.relay(group, selection, batchSize);
                } catch (RuntimeException exception) {
                    log.error("Outbox worker 배치 실행 실패: group={}", group.name(), exception);
                }
            }
        } catch (Error error) {
            synchronized (this) {
                slot.scheduled = false;
                slot.pending = false;
            }
            throw error;
        }
    }

    private synchronized boolean takeSignal(ExecutionSlot slot) {
        if (closed || !slot.pending) {
            slot.scheduled = false;
            return false;
        }
        slot.pending = false;
        return true;
    }

    // 실행 중인 배치는 완료하도록 두고 추가 선점은 중지한다.
    @Override
    public synchronized void close() {
        closed = true;
        slots.forEach(slot -> slot.pending = false);
    }

    private static final class ExecutionSlot {
        private boolean scheduled;
        private boolean pending;
    }
}
